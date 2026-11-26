package io.technoirlab.rhi.memory.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RangeChecksTest {
    @Test
    fun `span over an array range has the size of its elements in bytes`() {
        val whole = checkArraySpan(startIndex = 0, endIndex = 12, arraySize = 12, elementSize = 8)
        val part = checkArraySpan(startIndex = 3, endIndex = 5, arraySize = 12, elementSize = 4)
        val largest = checkArraySpan(startIndex = 0, endIndex = Int.MAX_VALUE / 8, arraySize = LARGE_ARRAY, elementSize = 8)

        assertEquals(listOf(96, 8, Int.MAX_VALUE - 7), listOf(whole, part, largest))
    }

    @Test
    fun `array range that holds more bytes than a span can is refused`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            checkArraySpan(startIndex = 0, endIndex = LARGE_ARRAY, arraySize = LARGE_ARRAY, elementSize = 8)
        }

        assertEquals("A range of 300000000 elements of 8 bytes holds more bytes than a span can, which is 2147483647", failure.message)
        assertFailsWith<IllegalArgumentException> { checkArraySpan(0, Int.MAX_VALUE / 8 + 1, LARGE_ARRAY, elementSize = 8) }
        assertFailsWith<IllegalArgumentException> { checkArraySpan(0, 1 shl 29, 1 shl 29, elementSize = 8) }
    }

    @Test
    fun `array range inside a larger array is checked by its own size`() {
        val size = checkArraySpan(startIndex = LARGE_ARRAY - 4, endIndex = LARGE_ARRAY, arraySize = LARGE_ARRAY, elementSize = 8)

        assertEquals(32, size)
    }

    @Test
    fun `transfer of an array range that holds more bytes than an Int can count is outside every span`() {
        val failure = assertFailsWith<IndexOutOfBoundsException> {
            checkArrayTransfer(offset = 4, startIndex = 0, endIndex = 1 shl 29, arraySize = 1 shl 29, elementSize = 8, spanSize = 64)
        }

        assertEquals("Range of 4294967296 bytes at offset 4 is outside a span of 64 bytes", failure.message)
    }

    @Test
    fun `range check holds at the largest span size`() {
        checkSpanRange(offset = 0, size = Int.MAX_VALUE, spanSize = Int.MAX_VALUE)
        checkSpanRange(offset = Int.MAX_VALUE, size = 0, spanSize = Int.MAX_VALUE)

        assertFailsWith<IndexOutOfBoundsException> { checkSpanRange(offset = 1, size = Int.MAX_VALUE, spanSize = Int.MAX_VALUE) }
        assertFailsWith<IndexOutOfBoundsException> { checkSpanRange(offset = Int.MAX_VALUE, size = Int.MAX_VALUE, spanSize = 8) }
        assertFailsWith<IndexOutOfBoundsException> { checkSpanRange(offset = Int.MIN_VALUE, size = 8, spanSize = 8) }
        assertFailsWith<IndexOutOfBoundsException> { checkSpanRange(offset = 0, size = Int.MIN_VALUE, spanSize = 8) }
    }

    private companion object {
        /**
         * The size of an array whose eight-byte elements take 2.4 GiB, more than a span can hold.
         */
        private const val LARGE_ARRAY = 300_000_000
    }
}
