package io.technoirlab.rhi.memory.allocator

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TlsfAllocatorTest {
    @Test
    fun `random allocations never overlap and keep alignment and statistics`() {
        for (seed in SEEDS) {
            val allocator = TlsfAllocator(CAPACITY)
            val live = runRandomSequence(allocator, Random(seed), OPERATIONS)

            freeAll(allocator, live, Random(seed))
            val whole = allocator.allocate(CAPACITY)

            assertFalse(whole.isNone, "seed $seed: the whole capacity is one free range again")
            assertEquals(0L, allocator.offsetOf(whole))
            assertEquals(CAPACITY, allocator.usedBytes)
        }
    }

    @Test
    fun `random allocations at sizes and alignments beyond 32 bits keep the same invariants`() {
        val capacity = 3L shl 40
        val allocator = TlsfAllocator(capacity)
        val live = runRandomSequence(
            allocator,
            Random(7),
            operations = 500,
            capacity = capacity,
            sizeScale = 1L shl 30,
            maxAlignmentLog2 = 36,
        )

        freeAll(allocator, live, Random(7))
        val whole = allocator.allocate(capacity)

        assertFalse(whole.isNone)
        assertEquals(capacity, allocator.sizeOf(whole))
    }

    @Test
    fun `freed handle is refused`() {
        val allocator = TlsfAllocator(1024)
        val allocation = allocator.allocate(100)
        allocator.free(allocation)

        assertFailsWith<IllegalArgumentException> { allocator.free(allocation) }
        assertFailsWith<IllegalArgumentException> { allocator.offsetOf(allocation) }
        assertFailsWith<IllegalArgumentException> { allocator.sizeOf(allocation) }
    }

    @Test
    fun `freed handle is refused after its node has been reused`() {
        val allocator = TlsfAllocator(1024)
        val first = allocator.allocate(100)
        allocator.free(first)

        val second = allocator.allocate(100)

        assertEquals(first.node, second.node, "the second allocation reuses the node of the first")
        assertFailsWith<IllegalArgumentException> { allocator.free(first) }
        assertFailsWith<IllegalArgumentException> { allocator.offsetOf(first) }
        assertEquals(100L, allocator.sizeOf(second))
        assertEquals(1, allocator.allocationCount)
    }

    @Test
    fun `freeing None does nothing`() {
        val allocator = TlsfAllocator(1024)
        allocator.allocate(10)

        allocator.free(OffsetAllocation.None)

        assertEquals(10L, allocator.usedBytes)
        assertEquals(1, allocator.allocationCount)
    }

    @Test
    fun `None has no offset or size`() {
        val allocator = TlsfAllocator(1024)

        val offsetFailure = assertFailsWith<IllegalArgumentException> { allocator.offsetOf(OffsetAllocation.None) }
        val sizeFailure = assertFailsWith<IllegalArgumentException> { allocator.sizeOf(OffsetAllocation.None) }

        assertEquals(NONE_MESSAGE, offsetFailure.message)
        assertEquals(NONE_MESSAGE, sizeFailure.message)
    }

    @Test
    fun `allocation that leaves no free range around it fits when the nodes have run out`() {
        val allocator = TlsfAllocator(capacity = 1024, maxNodes = 4)
        val first = allocator.allocate(1)
        allocator.allocate(1)
        allocator.allocate(1)
        val needsNode = allocator.allocate(1)
        allocator.free(first)

        val exactFit = allocator.allocate(1)

        assertTrue(needsNode.isNone, "the fourth allocation would split the free range, which takes a fifth node")
        assertFalse(exactFit.isNone)
        assertEquals(0L, allocator.offsetOf(exactFit))
        assertEquals(3, allocator.allocationCount)
    }

    @Test
    fun `allocation that does not fit returns None`() {
        val allocator = TlsfAllocator(100)

        val tooLarge = allocator.allocate(101)
        val all = allocator.allocate(100)
        val afterFull = allocator.allocate(1)

        assertTrue(tooLarge.isNone)
        assertFalse(all.isNone)
        assertTrue(afterFull.isNone)
        assertEquals(100L, allocator.usedBytes)
    }

    @Test
    fun `alignment padding stays free`() {
        val allocator = TlsfAllocator(1024)
        allocator.allocate(3)

        val aligned = allocator.allocate(16, alignment = 256)
        val padding = allocator.allocate(253)

        assertEquals(256L, allocator.offsetOf(aligned))
        assertEquals(3L, allocator.offsetOf(padding))
        assertEquals(272L, allocator.usedBytes)
    }

    @Test
    fun `aligned allocation close to the capacity fits an empty allocator`() {
        val capacity = 1L shl 20
        val allocator = TlsfAllocator(capacity)

        val allocation = allocator.allocate(capacity - 100, alignment = 256)

        assertFalse(allocation.isNone)
        assertEquals(0L, allocator.offsetOf(allocation))
    }

    @Test
    fun `aligned allocation takes a free range that starts aligned and is barely larger`() {
        val mib = 1L shl 20
        val allocator = TlsfAllocator(64 * mib)
        allocator.allocate(mib)

        val allocation = allocator.allocate(62 * mib + mib / 2, alignment = mib)

        assertFalse(allocation.isNone)
        assertEquals(mib, allocator.offsetOf(allocation))
    }

    @Test
    fun `aligned allocation passes over a range that its padding would push it out of`() {
        val allocator = TlsfAllocator(4096)
        allocator.allocate(8)
        val hole = allocator.allocate(292)
        allocator.allocate(8)
        allocator.free(hole)

        val aligned = allocator.allocate(280, alignment = 256)
        val unaligned = allocator.allocate(292)

        assertEquals(512L, allocator.offsetOf(aligned))
        assertEquals(8L, allocator.offsetOf(unaligned))
    }

    @Test
    fun `freed range merges with free neighbours on both sides`() {
        val allocator = TlsfAllocator(300)
        val first = allocator.allocate(100)
        val second = allocator.allocate(100)
        val third = allocator.allocate(100)

        allocator.free(first)
        allocator.free(third)
        allocator.free(second)
        val whole = allocator.allocate(300)

        assertFalse(whole.isNone)
        assertEquals(0L, allocator.offsetOf(whole))
    }

    @Test
    fun `invalid arguments are refused`() {
        val allocator = TlsfAllocator(1024)

        assertFailsWith<IllegalArgumentException> { TlsfAllocator(0) }
        assertFailsWith<IllegalArgumentException> { allocator.allocate(0) }
        assertFailsWith<IllegalArgumentException> { allocator.allocate(-1) }
        assertFailsWith<IllegalArgumentException> { allocator.allocate(16, alignment = 0) }
        assertFailsWith<IllegalArgumentException> { allocator.allocate(16, alignment = 48) }
    }

    private class Live(
        val allocation: OffsetAllocation,
        val offset: Long,
        val size: Long,
    )

    /**
     * Allocates and frees at random, checking every invariant along the way, and returns the allocations left alive.
     */
    private fun runRandomSequence(
        allocator: TlsfAllocator,
        random: Random,
        operations: Int,
        capacity: Long = CAPACITY,
        sizeScale: Long = 1,
        maxAlignmentLog2: Int = MAX_ALIGNMENT_LOG2,
    ): MutableList<Live> {
        val live = mutableListOf<Live>()
        var expectedUsed = 0L
        var failures = 0
        for (step in 0 until operations) {
            if (live.isEmpty() || random.nextInt(100) < ALLOCATE_PERCENT) {
                val size = randomSize(random, sizeScale)
                val alignment = 1L shl random.nextInt(maxAlignmentLog2 + 1)
                val allocation = allocator.allocate(size, alignment)
                if (allocation.isNone) {
                    failures++
                } else {
                    val offset = allocator.offsetOf(allocation)
                    assertEquals(0L, offset % alignment, "offset $offset is aligned to $alignment")
                    assertEquals(size, allocator.sizeOf(allocation))
                    assertTrue(offset >= 0 && offset <= capacity - size, "range $offset+$size lies inside the capacity")
                    live += Live(allocation, offset, size)
                    expectedUsed += size
                }
            } else {
                val index = random.nextInt(live.size)
                val freed = live[index]
                live[index] = live[live.lastIndex]
                live.removeAt(live.lastIndex)
                allocator.free(freed.allocation)
                expectedUsed -= freed.size
            }
            assertEquals(live.size, allocator.allocationCount)
            assertEquals(expectedUsed, allocator.usedBytes)
            if (step % OVERLAP_CHECK_INTERVAL == 0) assertNoOverlap(live)
        }
        assertNoOverlap(live)
        assertTrue(failures > 0, "the sequence fills the allocator at some point")
        return live
    }

    private fun freeAll(allocator: TlsfAllocator, live: MutableList<Live>, random: Random) {
        live.shuffle(random)
        for (allocation in live) {
            allocator.free(allocation.allocation)
        }
        assertEquals(0, allocator.allocationCount)
        assertEquals(0L, allocator.usedBytes)
    }

    private fun assertNoOverlap(live: List<Live>) {
        val sorted = live.sortedBy { it.offset }
        for (index in 1 until sorted.size) {
            val previous = sorted[index - 1]
            assertTrue(previous.offset + previous.size <= sorted[index].offset, "ranges do not overlap")
        }
    }

    /**
     * Mostly small sizes, some medium and a few large ones, so that the sequence both fragments and fills the space. A [scale]
     * above one multiplies the size and takes a random part of the last unit off it, so that ranges do not all start and end
     * on a multiple of the scale, where every alignment below it is met without padding.
     */
    private fun randomSize(random: Random, scale: Long): Long {
        val units = when (random.nextInt(10)) {
            in 0..5 -> random.nextLong(1, 257)
            in 6..8 -> random.nextLong(257, 16_385)
            else -> random.nextLong(16_385, 131_073)
        }
        return if (scale == 1L) units else units * scale - random.nextLong(scale)
    }

    private companion object {
        private const val CAPACITY = 1L shl 20
        private const val OPERATIONS = 3000
        private const val ALLOCATE_PERCENT = 55
        private const val MAX_ALIGNMENT_LOG2 = 8
        private const val OVERLAP_CHECK_INTERVAL = 50
        private val SEEDS = intArrayOf(1, 2, 3)
        private const val NONE_MESSAGE = "OffsetAllocation.None has no offset or size: the allocation that returned it failed"
    }
}
