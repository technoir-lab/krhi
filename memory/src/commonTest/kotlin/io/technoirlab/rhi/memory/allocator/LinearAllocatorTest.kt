package io.technoirlab.rhi.memory.allocator

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LinearAllocatorTest {
    @Test
    fun `allocations follow each other at their alignment`() {
        val allocator = LinearAllocator(1024)

        val offsets = listOf(
            allocator.allocate(3),
            allocator.allocate(8, alignment = 8),
            allocator.allocate(1),
            allocator.allocate(16, alignment = 256),
        )

        assertEquals(listOf(0L, 8L, 16L, 256L), offsets)
        assertEquals(272L, allocator.usedBytes)
    }

    @Test
    fun `allocation that does not fit returns NO_OFFSET and consumes nothing`() {
        val allocator = LinearAllocator(100)
        allocator.allocate(90)

        val tooLarge = allocator.allocate(11)
        val misaligned = allocator.allocate(4, alignment = 64)
        val exact = allocator.allocate(10)

        assertEquals(LinearAllocator.NO_OFFSET, tooLarge)
        assertEquals(LinearAllocator.NO_OFFSET, misaligned)
        assertEquals(-1L, LinearAllocator.NO_OFFSET)
        assertEquals(90L, exact)
        assertEquals(100L, allocator.usedBytes)
    }

    @Test
    fun `reset frees the whole space`() {
        val allocator = LinearAllocator(64)
        allocator.allocate(64)

        allocator.reset()
        val offset = allocator.allocate(32, alignment = 16)

        assertEquals(0L, offset)
        assertEquals(32L, allocator.usedBytes)
    }

    @Test
    fun `invalid arguments are refused`() {
        val allocator = LinearAllocator(64)

        assertFailsWith<IllegalArgumentException> { LinearAllocator(0) }
        assertFailsWith<IllegalArgumentException> { allocator.allocate(0) }
        assertFailsWith<IllegalArgumentException> { allocator.allocate(8, alignment = 3) }
    }
}
