package io.technoirlab.rhi.memory.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BuffersTest {
    @Test
    fun `range in the first 2 GiB of a longer buffer is accepted`() {
        checkBufferRange(FOUR_GIB, byteOffset = 1024, byteLength = 4096)
        checkBufferRange(FOUR_GIB, byteOffset = 0, byteLength = Int.MAX_VALUE)
    }

    @Test
    fun `range that ends past what a span can address in a longer buffer is refused`() {
        assertFailsWith<IllegalArgumentException> { checkBufferRange(FOUR_GIB, byteOffset = Int.MAX_VALUE, byteLength = 1) }
        assertFailsWith<IllegalArgumentException> { checkBufferRange(FOUR_GIB, byteOffset = 1, byteLength = Int.MAX_VALUE) }
    }

    @Test
    fun `rest of a buffer that is more than a span can hold is refused`() {
        val failure = assertFailsWith<IllegalArgumentException> { bytesAfter(FOUR_GIB, byteOffset = 16) }

        assertEquals("The rest of the buffer is 4294967280 bytes, more than a span can address, which is 2147483647", failure.message)
    }

    @Test
    fun `length that the platform gives is refused when a span cannot address it`() {
        val length = addressable(2147483647.0, "The view")

        assertEquals(Int.MAX_VALUE, length)
        assertFailsWith<IllegalArgumentException> { addressable(2147483648.0, "The view") }
        assertFailsWith<IllegalArgumentException> { addressable(FOUR_GIB, "The view") }
    }

    private companion object {
        /**
         * A buffer length that reads as zero when it is taken as an Int.
         */
        private const val FOUR_GIB = 4294967296.0
    }
}
