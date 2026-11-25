package io.technoirlab.rhi.memory

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MemorySpanTest {
    @Test
    fun `typed access reads back each type`() = withTestMemory(32) { span ->
        span.setByte(0, -2)
        span.setShort(1, -12345)
        span.setInt(3, 0x12345678)
        span.setLong(7, -0x123456789ABCDEFL)
        span.setFloat(15, 1.5f)
        span.setDouble(19, -2.25)

        val values = listOf(span.getByte(0), span.getShort(1), span.getInt(3), span.getLong(7), span.getFloat(15), span.getDouble(19))

        assertEquals(listOf<Any>((-2).toByte(), (-12345).toShort(), 0x12345678, -0x123456789ABCDEFL, 1.5f, -2.25), values)
    }

    @Test
    fun `typed access works at odd offsets from an odd base`() = withTestMemory(40) { memory ->
        val span = memory.slice(1)
        span.setShort(1, 0x0201)
        span.setInt(3, 0x06050403)
        span.setLong(7, 0x0E0D0C0B0A090807L)
        span.setFloat(15, Float.fromBits(0x1211100F))
        span.setDouble(19, Double.fromBits(0x1A19181716151413L))

        val bytes = memory.toByteArray()

        assertEquals(0x0201.toShort(), span.getShort(1))
        assertEquals(0x06050403, span.getInt(3))
        assertEquals(0x0E0D0C0B0A090807L, span.getLong(7))
        assertEquals(0x1211100F, span.getFloat(15).toRawBits())
        assertEquals(0x1A19181716151413L, span.getDouble(19).toRawBits())
        assertContentEquals(byteSequence(26), bytes.copyOfRange(2, 28))
    }

    @Test
    fun `typed access works at every alignment`() = withTestMemory(24) { memory ->
        for (shift in 0 until Long.SIZE_BYTES) {
            val span = memory.slice(shift)
            span.setShort(0, 0x0201)
            span.setInt(2, 0x06050403)
            span.setLong(6, 0x0E0D0C0B0A090807L)

            val bytes = span.slice(0, 14).toByteArray()
            val short = span.getShort(0)
            val int = span.getInt(2)
            val long = span.getLong(6)
            span.setFloat(0, 0.75f)
            span.setDouble(4, -0.125)
            val float = span.getFloat(0)
            val double = span.getDouble(4)

            assertContentEquals(byteSequence(14), bytes)
            assertEquals(listOf<Any>(0x0201.toShort(), 0x06050403, 0x0E0D0C0B0A090807L), listOf(short, int, long))
            assertEquals(listOf(0.75, -0.125), listOf(float.toDouble(), double))
        }
    }

    @Test
    fun `element access reaches the last bytes of the span`() = withTestMemory(8) { span ->
        span.setLong(0, 0x0807060504030201L)

        val last = listOf<Any>(span.getByte(7), span.getShort(6), span.getInt(4), span.getLong(0))

        assertEquals(listOf<Any>(8.toByte(), 0x0807.toShort(), 0x08070605, 0x0807060504030201L), last)
    }

    @Test
    fun `element access outside the span throws`() = withTestMemory(8) { span ->
        assertFailsWith<IndexOutOfBoundsException> { span.getByte(8) }
        assertFailsWith<IndexOutOfBoundsException> { span.getShort(7) }
        assertFailsWith<IndexOutOfBoundsException> { span.getInt(5) }
        assertFailsWith<IndexOutOfBoundsException> { span.getLong(-1) }
        assertFailsWith<IndexOutOfBoundsException> { span.getFloat(Int.MAX_VALUE) }
        assertFailsWith<IndexOutOfBoundsException> { span.getDouble(1) }
        assertFailsWith<IndexOutOfBoundsException> { span.setByte(-1, 0) }
        assertFailsWith<IndexOutOfBoundsException> { span.setShort(7, 0) }
        assertFailsWith<IndexOutOfBoundsException> { span.setInt(8, 0) }
        assertFailsWith<IndexOutOfBoundsException> { span.setLong(1, 0) }
        assertFailsWith<IndexOutOfBoundsException> { span.setFloat(5, 0f) }
        assertFailsWith<IndexOutOfBoundsException> { span.setDouble(1, 0.0) }
    }

    @Test
    fun `element access outside the span leaves it unchanged`() = withTestMemory(8) { span ->
        span.write(byteSequence(8))

        assertFailsWith<IndexOutOfBoundsException> { span.setLong(4, -1) }
        assertFailsWith<IndexOutOfBoundsException> { span.setInt(6, -1) }

        assertContentEquals(byteSequence(8), span.toByteArray())
    }

    @Test
    fun `toString names the type and the size`() = withTestMemory(16) { memory ->
        val text = memory.slice(4, 8).toString()

        assertTrue(text.startsWith("MutableMemorySpan(") && text.endsWith("size=8)"), text)
    }

    @Test
    fun `slice reads the memory of the span at its offset`() = withTestMemory(16) { memory ->
        memory.write(byteSequence(16, first = 0))

        val slice = memory.slice(4, 8)

        assertEquals(8, slice.size)
        assertContentEquals(byteSequence(8, first = 4), slice.toByteArray())
    }

    @Test
    fun `slice without a size reaches the end of the span`() = withTestMemory(16) { memory ->
        memory.write(byteSequence(16, first = 0))

        val slice = memory.slice(10)

        assertContentEquals(byteSequence(6, first = 10), slice.toByteArray())
    }

    @Test
    fun `slice of a slice is relative to the slice`() = withTestMemory(16) { memory ->
        memory.write(byteSequence(16, first = 0))

        val slice = memory.slice(3, 10).slice(2, 4)

        assertContentEquals(byteSequence(4, first = 5), slice.toByteArray())
    }

    @Test
    fun `slice outside the span throws`() = withTestMemory(16) { memory ->
        val span: MemorySpan = memory

        assertFailsWith<IndexOutOfBoundsException> { span.slice(-1, 1) }
        assertFailsWith<IndexOutOfBoundsException> { span.slice(0, 17) }
        assertFailsWith<IndexOutOfBoundsException> { span.slice(17) }
        assertFailsWith<IndexOutOfBoundsException> { span.slice(8, -1) }
        assertFailsWith<IndexOutOfBoundsException> { span.slice(16, 1) }
    }

    @Test
    fun `offset outside the span is reported without the size it gave by default`() = withTestMemory(8) { memory ->
        val failure = assertFailsWith<IndexOutOfBoundsException> { memory.slice(10) }

        assertEquals("Offset 10 is outside a span of 8 bytes", failure.message)
    }

    @Test
    fun `copyTo copies the whole span by default`() = withTestMemory(24) { memory ->
        val source = memory.slice(0, 8)
        val destination = memory.slice(8, 16)
        source.write(byteSequence(8))

        source.copyTo(destination)

        assertContentEquals(byteSequence(8), destination.slice(0, 8).toByteArray())
    }

    @Test
    fun `copyTo copies a range to an offset in the destination`() = withTestMemory(24) { memory ->
        val source = memory.slice(0, 8)
        val destination = memory.slice(8, 16)
        source.write(byteSequence(8))
        destination.fill(0)

        source.copyTo(destination, destinationOffset = 5, offset = 2, size = 3)

        assertContentEquals(byteArrayOf(0, 0, 0, 0, 0, 3, 4, 5, 0), destination.slice(0, 9).toByteArray())
    }

    @Test
    fun `copyTo copies between overlapping ranges of the same memory`() = withTestMemory(8) { memory ->
        memory.write(byteSequence(8))

        memory.copyTo(memory, destinationOffset = 2, offset = 0, size = 6)

        assertContentEquals(byteArrayOf(1, 2, 1, 2, 3, 4, 5, 6), memory.toByteArray())
    }

    @Test
    fun `copyTo outside either span throws`() = withTestMemory(16) { memory ->
        val source = memory.slice(0, 8)
        val destination = memory.slice(8, 8)

        assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination, offset = 4, size = 5) }
        assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination, offset = -1, size = 2) }
        assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination, destinationOffset = 4, size = 5) }
        assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination, destinationOffset = -1, size = 2) }
        assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination, size = -1) }
        assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination.slice(0, 4)) }
    }

    @Test
    fun `copyTo names the span that the range leaves`() = withTestMemory(16) { memory ->
        val source = memory.slice(0, 8)
        val destination = memory.slice(8, 4)

        val sourceFailure = assertFailsWith<IndexOutOfBoundsException> { source.copyTo(memory, offset = 4, size = 5) }
        val destinationFailure = assertFailsWith<IndexOutOfBoundsException> { source.copyTo(destination) }

        assertEquals("Range of 5 bytes at offset 4 is outside the source span of 8 bytes", sourceFailure.message)
        assertEquals("Range of 8 bytes at offset 0 is outside the destination span of 4 bytes", destinationFailure.message)
    }

    @Test
    fun `read fills the whole array by default`() = withTestMemory(48) { memory ->
        val span = memory.slice(1)
        span.write(byteSequence(47))
        val bytes = ByteArray(5)
        val shorts = ShortArray(3)
        val ints = IntArray(3)
        val longs = LongArray(2)
        val floats = FloatArray(3)
        val doubles = DoubleArray(2)

        span.read(bytes)
        span.read(shorts, offset = 1)
        span.read(ints, offset = 3)
        span.read(longs, offset = 5)
        span.read(floats, offset = 7)
        span.read(doubles, offset = 9)

        assertContentEquals(byteSequence(5), bytes)
        assertContentEquals(ShortArray(3) { span.getShort(1 + it * 2) }, shorts)
        assertContentEquals(IntArray(3) { span.getInt(3 + it * 4) }, ints)
        assertContentEquals(LongArray(2) { span.getLong(5 + it * 8) }, longs)
        assertContentEquals(IntArray(3) { span.getInt(7 + it * 4) }, IntArray(3) { floats[it].toRawBits() })
        assertContentEquals(LongArray(2) { span.getLong(9 + it * 8) }, LongArray(2) { doubles[it].toRawBits() })
    }

    @Test
    fun `read fills a range of the array`() = withTestMemory(32) { memory ->
        memory.write(intArrayOf(10, 20, 30, 40, 50, 60, 70, 80))
        val ints = IntArray(6) { -1 }
        val longs = LongArray(4) { -1 }

        memory.read(ints, offset = 4, startIndex = 2, endIndex = 5)
        memory.read(longs, offset = 8, startIndex = 1, endIndex = 3)

        assertContentEquals(intArrayOf(-1, -1, 20, 30, 40, -1), ints)
        assertContentEquals(longArrayOf(-1, memory.getLong(8), memory.getLong(16), -1), longs)
    }

    @Test
    fun `read outside the span or the array throws`() = withTestMemory(16) { memory ->
        val span: MemorySpan = memory

        assertFailsWith<IndexOutOfBoundsException> { span.read(ByteArray(16), offset = 1) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(ByteArray(1), offset = -1) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(ShortArray(8), offset = 2) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(IntArray(4), offset = 4) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(LongArray(2), offset = 8) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(FloatArray(4), offset = 4) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(DoubleArray(2), offset = 8) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(IntArray(4), startIndex = -1) }
        assertFailsWith<IndexOutOfBoundsException> { span.read(IntArray(4), endIndex = 5) }
        assertFailsWith<IllegalArgumentException> { span.read(IntArray(4), startIndex = 3, endIndex = 2) }
    }

    @Test
    fun `operations on a zero-length span touch no memory`() = withTestMemory(8) { memory ->
        memory.write(byteSequence(8))
        val empty = memory.slice(8)

        empty.copyTo(memory, destinationOffset = 8)
        memory.copyTo(empty, offset = 8)
        empty.fill(0)
        empty.read(ByteArray(0))
        empty.write(DoubleArray(0))
        empty.read(LongArray(4), startIndex = 4)
        empty.write(FloatArray(4), startIndex = 2, endIndex = 2)

        assertEquals(0, empty.size)
        assertContentEquals(byteSequence(8), memory.toByteArray())
    }

    @Test
    fun `empty array ranges transfer nothing at the end of the span`() = withTestMemory(8) { memory ->
        memory.write(byteSequence(8))
        val shorts = shortArrayOf(7, 7)

        memory.read(shorts, offset = 8, startIndex = 2)
        memory.write(IntArray(3), offset = 8, startIndex = 3)
        memory.read(ByteArray(0), offset = 8)
        memory.write(LongArray(0), offset = 8)

        assertContentEquals(shortArrayOf(7, 7), shorts)
        assertContentEquals(byteSequence(8), memory.toByteArray())
    }

    @Test
    fun `array transfer moves arrays larger than one chunk`() = withTestMemory(100_003) { memory ->
        val span = memory.slice(3)
        val source = IntArray(25_000) { it * 7919 }
        val result = IntArray(25_000)

        span.write(source)
        span.read(result)

        assertContentEquals(source, result)
        assertEquals(source.last(), span.getInt(99_996))
    }
}
