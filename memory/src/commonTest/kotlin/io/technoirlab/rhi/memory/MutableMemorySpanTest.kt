package io.technoirlab.rhi.memory

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MutableMemorySpanTest {
    @Test
    fun `typed writes are little-endian`() = withTestMemory(30) { span ->
        span.setShort(0, 0x0201)
        span.setInt(2, 0x06050403)
        span.setLong(6, 0x0E0D0C0B0A090807L)
        span.setFloat(14, Float.fromBits(0x1211100F))
        span.setDouble(18, Double.fromBits(0x1A19181716151413L))
        span.setByte(26, 0x1B)

        val bytes = span.slice(0, 27).toByteArray()

        assertContentEquals(byteSequence(27), bytes)
    }

    @Test
    fun `array writes use the byte order of typed access`() = withTestMemory(64) { span ->
        span.write(byteArrayOf(1, 2))
        span.write(shortArrayOf(0x0403, 0x0605), offset = 2)
        span.write(intArrayOf(0x0A090807), offset = 6)
        span.write(longArrayOf(0x1211100F0E0D0C0BL), offset = 10)
        span.write(floatArrayOf(Float.fromBits(0x16151413)), offset = 18)
        span.write(doubleArrayOf(Double.fromBits(0x1E1D1C1B1A191817L)), offset = 22)

        val bytes = span.slice(0, 30).toByteArray()

        assertContentEquals(byteSequence(30), bytes)
        assertEquals(0x0201.toShort(), span.getShort(0))
        assertEquals(0x0605.toShort(), span.getShort(4))
        assertEquals(0x0A090807, span.getInt(6))
        assertEquals(0x1211100F0E0D0C0BL, span.getLong(10))
        assertEquals(0x16151413, span.getFloat(18).toRawBits())
        assertEquals(0x1E1D1C1B1A191817L, span.getDouble(22).toRawBits())
    }

    @Test
    fun `array reads use the byte order of typed access`() = withTestMemory(64) { span ->
        span.write(byteSequence(64))
        val shorts = ShortArray(2)
        val ints = IntArray(2)
        val longs = LongArray(2)
        val floats = FloatArray(2)
        val doubles = DoubleArray(2)

        span.read(shorts, offset = 1)
        span.read(ints, offset = 5)
        span.read(longs, offset = 13)
        span.read(floats, offset = 29)
        span.read(doubles, offset = 37)

        assertContentEquals(shortArrayOf(0x0302, 0x0504), shorts)
        assertContentEquals(intArrayOf(0x09080706, 0x0D0C0B0A), ints)
        assertContentEquals(longArrayOf(0x1514131211100F0EL, 0x1D1C1B1A19181716L), longs)
        assertContentEquals(intArrayOf(0x21201F1E, 0x25242322), IntArray(2) { floats[it].toRawBits() })
        assertContentEquals(longArrayOf(0x2D2C2B2A29282726L, 0x3534333231302F2EL), LongArray(2) { doubles[it].toRawBits() })
    }

    @Test
    fun `write copies a range of the array`() = withTestMemory(32) { span ->
        span.fill(0)

        span.write(intArrayOf(10, 20, 30, 40), offset = 4, startIndex = 1, endIndex = 3)
        span.write(shortArrayOf(1, 2, 3, 4), offset = 16, startIndex = 2)
        span.write(byteArrayOf(5, 6, 7), offset = 24, endIndex = 1)

        assertEquals(listOf(0, 20, 30, 0), List(4) { span.getInt(it * 4) })
        assertEquals(listOf<Short>(3, 4, 0), List(3) { span.getShort(16 + it * 2) })
        assertEquals(listOf<Byte>(5, 0), List(2) { span.getByte(24 + it) })
    }

    @Test
    fun `write outside the span or the array throws`() = withTestMemory(16) { span ->
        assertFailsWith<IndexOutOfBoundsException> { span.write(ByteArray(16), offset = 1) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(ByteArray(1), offset = -1) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(ShortArray(8), offset = 2) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(IntArray(4), offset = 4) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(LongArray(2), offset = 8) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(FloatArray(4), offset = 4) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(DoubleArray(2), offset = 8) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(IntArray(4), startIndex = -1) }
        assertFailsWith<IndexOutOfBoundsException> { span.write(IntArray(4), endIndex = 5) }
        assertFailsWith<IllegalArgumentException> { span.write(IntArray(4), startIndex = 3, endIndex = 2) }
    }

    @Test
    fun `fill sets the whole span by default`() = withTestMemory(8) { span ->
        span.fill(0x5A)

        val bytes = span.toByteArray()

        assertContentEquals(ByteArray(8) { 0x5A }, bytes)
    }

    @Test
    fun `fill sets a range`() = withTestMemory(8) { span ->
        span.fill(0)

        span.fill(-1, offset = 2, size = 3)

        assertContentEquals(byteArrayOf(0, 0, -1, -1, -1, 0, 0, 0), span.toByteArray())
    }

    @Test
    fun `fill from an offset reaches the end of the span`() = withTestMemory(8) { span ->
        span.fill(0)

        span.fill(7, offset = 5)

        assertContentEquals(byteArrayOf(0, 0, 0, 0, 0, 7, 7, 7), span.toByteArray())
    }

    @Test
    fun `fill outside the span throws`() = withTestMemory(8) { span ->
        assertFailsWith<IndexOutOfBoundsException> { span.fill(0, offset = -1, size = 1) }
        assertFailsWith<IndexOutOfBoundsException> { span.fill(0, offset = 4, size = 5) }
        assertFailsWith<IndexOutOfBoundsException> { span.fill(0, offset = 9) }
        assertFailsWith<IndexOutOfBoundsException> { span.fill(0, size = -1) }
    }

    @Test
    fun `mutable slice writes the memory of the span`() = withTestMemory(8) { memory ->
        memory.fill(0)

        memory.slice(2, 4).setInt(0, 0x04030201)

        assertContentEquals(byteArrayOf(0, 0, 1, 2, 3, 4, 0, 0), memory.toByteArray())
    }
}
