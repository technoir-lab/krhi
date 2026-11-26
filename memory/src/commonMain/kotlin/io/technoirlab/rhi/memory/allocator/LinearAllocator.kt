package io.technoirlab.rhi.memory.allocator

/**
 * A bump allocator of ranges in the abstract space `[0, capacity)`, with no individual free. It touches no memory.
 *
 * Each allocation starts at the first suitably aligned offset after the previous one; [reset] makes the whole space free again.
 *
 * The allocator is not synchronised. Callers that share one between threads lock around it.
 *
 * @throws IllegalArgumentException when [capacity] is not positive.
 */
class LinearAllocator(val capacity: Long) {
    /**
     * The number of bytes consumed since the last [reset], alignment padding included.
     */
    var usedBytes = 0L
        private set

    init {
        require(capacity > 0) { "Capacity must be positive: $capacity" }
    }

    /**
     * Allocates [size] bytes at an offset that is a multiple of [alignment], a power of two.
     *
     * @return the offset, or [NO_OFFSET] when the rest of the space is too small.
     * @throws IllegalArgumentException when [size] is not positive or [alignment] is not a power of two.
     */
    fun allocate(size: Long, alignment: Long = 1): Long {
        require(size > 0) { "Size must be positive: $size" }
        require(alignment > 0 && alignment and (alignment - 1) == 0L) { "Alignment must be a power of two: $alignment" }
        val padding = (alignment - (usedBytes and (alignment - 1))) and (alignment - 1)
        if (padding > capacity - usedBytes || size > capacity - usedBytes - padding) return NO_OFFSET
        val offset = usedBytes + padding
        usedBytes = offset + size
        return offset
    }

    /**
     * Makes the whole space free again. Offsets handed out before stop being reserved.
     */
    fun reset() {
        usedBytes = 0
    }

    companion object {
        /**
         * What [allocate] returns in place of an offset when the allocation does not fit. It is -1: no offset is equal to
         * it, and every offset is greater.
         */
        const val NO_OFFSET = -1L
    }
}
