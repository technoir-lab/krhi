package io.technoirlab.rhi.memory.allocator

/**
 * A two-level segregated fit allocator of ranges in the abstract space `[0, capacity)`. It touches no memory.
 *
 * Allocating and freeing take constant time: size classes are found with bit scans over two levels of bitmaps. A freed range
 * is merged with free neighbours at once, and an allocation is exactly the size that was asked for: alignment padding and the
 * remainder of the block it came from stay free. Bookkeeping lives in primitive arrays, so allocating and freeing create no
 * objects apart from the occasional growth of those arrays.
 *
 * The search looks at the first free range of a few size classes and at nothing else, which is what keeps it constant-time.
 * It can therefore miss a range that would fit, and does so mostly when the space is nearly full.
 *
 * An [OffsetAllocation] is valid only with the allocator that returned it. The allocator recognises its own freed handles,
 * but cannot tell another allocator's handle from one of its own.
 *
 * The allocator is not synchronised. Callers that share one between threads lock around it.
 */
class TlsfAllocator internal constructor(
    val capacity: Long,
    private val maxNodes: Int,
) {
    private var offsets = LongArray(INITIAL_NODES)
    private var sizes = LongArray(INITIAL_NODES)
    private var previousPhysical = IntArray(INITIAL_NODES)
    private var nextPhysical = IntArray(INITIAL_NODES)
    private var previousFree = IntArray(INITIAL_NODES)
    private var nextFree = IntArray(INITIAL_NODES)
    private var generations = IntArray(INITIAL_NODES)
    private var states = ByteArray(INITIAL_NODES)

    /**
     * Nodes ever created; the indices below it are in use or in the pool of unused nodes.
     */
    private var nodeCount = 0
    private var unusedHead = NONE
    private var unusedCount = 0

    private val freeHeads = IntArray(FIRST_LEVEL_COUNT * SECOND_LEVEL_COUNT) { NONE }
    private val secondLevelBitmaps = IntArray(FIRST_LEVEL_COUNT)
    private var firstLevelBitmapLow = 0
    private var firstLevelBitmapHigh = 0

    /**
     * The number of bytes in live allocations.
     */
    var usedBytes = 0L
        private set

    /**
     * The number of live allocations.
     */
    var allocationCount = 0
        private set

    init {
        require(capacity > 0) { "Capacity must be positive: $capacity" }
        require(maxNodes in 1..OffsetAllocation.MAX_NODES) { "Node limit must be in 1..${OffsetAllocation.MAX_NODES}: $maxNodes" }
        val node = takeNode()
        offsets[node] = 0
        sizes[node] = capacity
        previousPhysical[node] = NONE
        nextPhysical[node] = NONE
        insertFree(node)
    }

    /**
     * Creates an allocator of ranges in `[0, capacity)`.
     *
     * @throws IllegalArgumentException when [capacity] is not positive.
     */
    constructor(capacity: Long) : this(capacity, OffsetAllocation.MAX_NODES)

    /**
     * Allocates [size] bytes at an offset that is a multiple of [alignment], a power of two.
     *
     * @return the allocation, or [OffsetAllocation.None] when the search finds no free range that fits, or when the allocator
     * has no node left for a part of the range found that would stay free.
     * @throws IllegalArgumentException when [size] is not positive or [alignment] is not a power of two.
     */
    fun allocate(size: Long, alignment: Long = 1): OffsetAllocation {
        require(size > 0) { "Size must be positive: $size" }
        require(alignment > 0 && alignment and (alignment - 1) == 0L) { "Alignment must be a power of two: $alignment" }
        if (size > capacity) return OffsetAllocation.None
        val block = findFreeBlock(size, alignment)
        if (block == NONE) return OffsetAllocation.None
        // Each part of the block that stays free takes a node: the padding in front of the allocation and the rest behind it.
        val padding = paddingOf(offsets[block], alignment)
        val rest = sizes[block] - padding - size
        val newNodes = (if (padding != 0L) 1 else 0) + (if (rest != 0L) 1 else 0)
        if (!canTakeNodes(newNodes)) return OffsetAllocation.None
        removeFree(block)

        var node = block
        if (padding != 0L) {
            node = splitAfter(block, padding)
            insertFree(block)
        }
        if (rest != 0L) {
            insertFree(splitAfter(node, size))
        }
        states[node] = USED
        usedBytes += size
        allocationCount++
        return OffsetAllocation.of(node, generations[node])
    }

    /**
     * Frees [allocation] and merges its range with free neighbours. Freeing [OffsetAllocation.None] does nothing.
     *
     * @throws IllegalArgumentException when the allocation has already been freed.
     */
    fun free(allocation: OffsetAllocation) {
        if (allocation.isNone) return
        var node = checkLive(allocation)
        generations[node] = (generations[node] + 1) and OffsetAllocation.GENERATION_MASK
        usedBytes -= sizes[node]
        allocationCount--

        val previous = previousPhysical[node]
        if (previous != NONE && states[previous] == FREE) {
            removeFree(previous)
            absorbNext(previous)
            node = previous
        }
        val next = nextPhysical[node]
        if (next != NONE && states[next] == FREE) {
            removeFree(next)
            absorbNext(node)
        }
        insertFree(node)
    }

    /**
     * Returns the offset of [allocation].
     *
     * @throws IllegalArgumentException when the allocation has been freed or is [OffsetAllocation.None].
     */
    fun offsetOf(allocation: OffsetAllocation): Long = offsets[checkLive(allocation)]

    /**
     * Returns the size of [allocation], which is the size that was asked for.
     *
     * @throws IllegalArgumentException when the allocation has been freed or is [OffsetAllocation.None].
     */
    fun sizeOf(allocation: OffsetAllocation): Long = sizes[checkLive(allocation)]

    private fun checkLive(allocation: OffsetAllocation): Int {
        require(!allocation.isNone) { "OffsetAllocation.None has no offset or size: the allocation that returned it failed" }
        val node = allocation.node
        require(node < nodeCount && states[node] == USED && generations[node] == allocation.generation) {
            "$allocation is not live in this allocator: it has been freed, or another allocator returned it"
        }
        return node
    }

    /**
     * Returns a free block that can hold [size] bytes at [alignment], or [NONE].
     */
    private fun findFreeBlock(size: Long, alignment: Long): Int {
        // The first block of the request's own class may fit, which the rounded search below would skip: without this, a
        // request for exactly the size of the only free block fails unless that size happens to start a class.
        val firstLevel = firstLevelOf(size)
        val head = freeHeads[firstLevel * SECOND_LEVEL_COUNT + secondLevelOf(size, firstLevel)]
        if (head != NONE && fits(head, size, alignment)) return head
        // The first block that is large enough fits unless the padding in front of an aligned start pushes the request past its
        // end. When no block is large enough, none is large enough for the padding either.
        val block = findBlockOfAtLeast(size)
        if (block == NONE || fits(block, size, alignment)) return block
        // Searching for alignment - 1 more bytes guarantees room for the padding.
        if (alignment - 1 > capacity - size) return NONE
        return findBlockOfAtLeast(size + (alignment - 1))
    }

    private fun fits(node: Int, size: Long, alignment: Long): Boolean = paddingOf(offsets[node], alignment) <= sizes[node] - size

    /**
     * Returns the head of a free list whose blocks are all at least [size] bytes, or [NONE].
     */
    private fun findBlockOfAtLeast(size: Long): Int {
        // Rounding up to the next class boundary makes every block in the class found, and in the classes above it, fit.
        val granularity = if (size < SECOND_LEVEL_COUNT) 1L else 1L shl (highestBit(size) - SECOND_LEVEL_LOG2)
        if (size > Long.MAX_VALUE - (granularity - 1)) return NONE
        val rounded = size + (granularity - 1)
        var firstLevel = firstLevelOf(rounded)
        var secondLevelMap = secondLevelBitmaps[firstLevel] and (-1 shl secondLevelOf(rounded, firstLevel))
        if (secondLevelMap == 0) {
            firstLevel = lowestFirstLevelAbove(firstLevel)
            if (firstLevel == NONE) return NONE
            secondLevelMap = secondLevelBitmaps[firstLevel]
        }
        return freeHeads[firstLevel * SECOND_LEVEL_COUNT + secondLevelMap.countTrailingZeroBits()]
    }

    private fun lowestFirstLevelAbove(firstLevel: Int): Int {
        val next = firstLevel + 1
        if (next < Int.SIZE_BITS) {
            val low = firstLevelBitmapLow and (-1 shl next)
            if (low != 0) return low.countTrailingZeroBits()
            return if (firstLevelBitmapHigh != 0) Int.SIZE_BITS + firstLevelBitmapHigh.countTrailingZeroBits() else NONE
        }
        val high = firstLevelBitmapHigh and (-1 shl (next - Int.SIZE_BITS))
        return if (high != 0) Int.SIZE_BITS + high.countTrailingZeroBits() else NONE
    }

    private fun insertFree(node: Int) {
        val size = sizes[node]
        val firstLevel = firstLevelOf(size)
        val secondLevel = secondLevelOf(size, firstLevel)
        val list = firstLevel * SECOND_LEVEL_COUNT + secondLevel
        val head = freeHeads[list]
        previousFree[node] = NONE
        nextFree[node] = head
        if (head != NONE) previousFree[head] = node
        freeHeads[list] = node
        states[node] = FREE
        secondLevelBitmaps[firstLevel] = secondLevelBitmaps[firstLevel] or (1 shl secondLevel)
        setFirstLevelBit(firstLevel)
    }

    private fun removeFree(node: Int) {
        val previous = previousFree[node]
        val next = nextFree[node]
        if (next != NONE) previousFree[next] = previous
        if (previous != NONE) {
            nextFree[previous] = next
            return
        }
        val size = sizes[node]
        val firstLevel = firstLevelOf(size)
        val secondLevel = secondLevelOf(size, firstLevel)
        freeHeads[firstLevel * SECOND_LEVEL_COUNT + secondLevel] = next
        if (next == NONE) {
            val map = secondLevelBitmaps[firstLevel] and (1 shl secondLevel).inv()
            secondLevelBitmaps[firstLevel] = map
            if (map == 0) clearFirstLevelBit(firstLevel)
        }
    }

    private fun setFirstLevelBit(firstLevel: Int) {
        if (firstLevel < Int.SIZE_BITS) {
            firstLevelBitmapLow = firstLevelBitmapLow or (1 shl firstLevel)
        } else {
            firstLevelBitmapHigh = firstLevelBitmapHigh or (1 shl (firstLevel - Int.SIZE_BITS))
        }
    }

    private fun clearFirstLevelBit(firstLevel: Int) {
        if (firstLevel < Int.SIZE_BITS) {
            firstLevelBitmapLow = firstLevelBitmapLow and (1 shl firstLevel).inv()
        } else {
            firstLevelBitmapHigh = firstLevelBitmapHigh and (1 shl (firstLevel - Int.SIZE_BITS)).inv()
        }
    }

    /**
     * Splits [node] after its first [firstSize] bytes and returns the new node for the rest, which follows it in address order.
     */
    private fun splitAfter(node: Int, firstSize: Long): Int {
        val rest = takeNode()
        offsets[rest] = offsets[node] + firstSize
        sizes[rest] = sizes[node] - firstSize
        sizes[node] = firstSize
        val next = nextPhysical[node]
        previousPhysical[rest] = node
        nextPhysical[rest] = next
        nextPhysical[node] = rest
        if (next != NONE) previousPhysical[next] = rest
        return rest
    }

    /**
     * Merges the node that follows [node] in address order into it, and returns the merged node to the pool.
     */
    private fun absorbNext(node: Int) {
        val next = nextPhysical[node]
        sizes[node] += sizes[next]
        val after = nextPhysical[next]
        nextPhysical[node] = after
        if (after != NONE) previousPhysical[after] = node
        releaseNode(next)
    }

    private fun canTakeNodes(count: Int): Boolean = unusedCount + (maxNodes - nodeCount) >= count

    private fun takeNode(): Int {
        if (unusedHead != NONE) {
            val node = unusedHead
            unusedHead = nextFree[node]
            unusedCount--
            return node
        }
        if (nodeCount == states.size) grow()
        return nodeCount++
    }

    private fun releaseNode(node: Int) {
        states[node] = UNUSED
        nextFree[node] = unusedHead
        unusedHead = node
        unusedCount++
    }

    private fun grow() {
        val newSize = minOf(states.size * 2, maxNodes)
        offsets = offsets.copyOf(newSize)
        sizes = sizes.copyOf(newSize)
        previousPhysical = previousPhysical.copyOf(newSize)
        nextPhysical = nextPhysical.copyOf(newSize)
        previousFree = previousFree.copyOf(newSize)
        nextFree = nextFree.copyOf(newSize)
        generations = generations.copyOf(newSize)
        states = states.copyOf(newSize)
    }

    private companion object {
        private const val NONE = -1
        private const val INITIAL_NODES = 64

        private const val UNUSED: Byte = 0
        private const val FREE: Byte = 1
        private const val USED: Byte = 2

        /**
         * Each power-of-two range of sizes is divided into 32 classes, so a second-level bitmap is one Int.
         */
        private const val SECOND_LEVEL_LOG2 = 5
        private const val SECOND_LEVEL_COUNT = 1 shl SECOND_LEVEL_LOG2

        /**
         * Sizes below [SECOND_LEVEL_COUNT] share first level 0; above it, each power of two up to 2^62 has a first level.
         */
        private const val FIRST_LEVEL_COUNT = Long.SIZE_BITS - 1 - SECOND_LEVEL_LOG2 + 1

        /**
         * Returns the bytes from [offset] up to the next multiple of [alignment], a power of two.
         */
        private fun paddingOf(offset: Long, alignment: Long): Long = (alignment - (offset and (alignment - 1))) and (alignment - 1)

        private fun highestBit(size: Long): Int = Long.SIZE_BITS - 1 - size.countLeadingZeroBits()

        private fun firstLevelOf(size: Long): Int = if (size < SECOND_LEVEL_COUNT) 0 else highestBit(size) - SECOND_LEVEL_LOG2 + 1

        private fun secondLevelOf(size: Long, firstLevel: Int): Int =
            if (firstLevel == 0) size.toInt() else (size ushr (firstLevel - 1)).toInt() - SECOND_LEVEL_COUNT
    }
}
