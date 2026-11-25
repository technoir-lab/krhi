package io.technoirlab.rhi.memory

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UseSpanTest {
    @Test
    fun `useSpan gives the bytes of each array type in little-endian order`() {
        val bytes = byteArrayOf(1, 2)
        val shorts = shortArrayOf(0x0403, 0x0605)
        val ints = intArrayOf(0x0A090807)
        val longs = longArrayOf(0x1211100F0E0D0C0BL)
        val floats = floatArrayOf(Float.fromBits(0x16151413))
        val doubles = doubleArrayOf(Double.fromBits(0x1E1D1C1B1A191817L))

        val result = listOf(
            bytes.useSpan { it.toByteArray() },
            shorts.useSpan { it.toByteArray() },
            ints.useSpan { it.toByteArray() },
            longs.useSpan { it.toByteArray() },
            floats.useSpan { it.toByteArray() },
            doubles.useSpan { it.toByteArray() },
        ).reduce { all, next -> all + next }

        assertContentEquals(byteSequence(30), result)
    }

    @Test
    fun `useSpan calls its block in place so that the block can initialise a value`() {
        val byte: Byte
        val short: Short
        val int: Int
        val long: Long
        val float: Float
        val double: Double

        byteArrayOf(1).useSpan { byte = it.getByte(0) }
        shortArrayOf(2).useSpan { short = it.getShort(0) }
        intArrayOf(3).useSpan { int = it.getInt(0) }
        longArrayOf(4).useSpan { long = it.getLong(0) }
        floatArrayOf(5f).useSpan { float = it.getFloat(0) }
        doubleArrayOf(6.0).useSpan { double = it.getDouble(0) }

        assertEquals(listOf<Any>(1.toByte(), 2.toShort(), 3, 4L, 5f, 6.0), listOf<Any>(byte, short, int, long, float, double))
    }

    @Test
    fun `useSpan gives a span that cannot be cast to a writable one`() {
        val writable = listOf(
            ByteArray(2).useSpan { it is MutableMemorySpan },
            ShortArray(2).useSpan { it is MutableMemorySpan },
            IntArray(2).useSpan { it is MutableMemorySpan },
            LongArray(2).useSpan { it is MutableMemorySpan },
            FloatArray(2).useSpan { it is MutableMemorySpan },
            DoubleArray(2).useSpan { it is MutableMemorySpan },
            IntArray(0).useSpan { it is MutableMemorySpan },
        )

        assertEquals(List(7) { false }, writable)
    }

    @Test
    fun `useSpan covers a range of the array`() {
        val ints = intArrayOf(10, 20, 30, 40, 50)
        val doubles = doubleArrayOf(1.0, 2.0, 3.0)

        val intValues = ints.useSpan(startIndex = 1, endIndex = 4) { span -> List(span.size / 4) { span.getInt(it * 4) } }
        val doubleValues = doubles.useSpan(startIndex = 2) { span -> List(span.size / 8) { span.getDouble(it * 8) } }

        assertEquals(listOf(20, 30, 40), intValues)
        assertEquals(listOf(3.0), doubleValues)
    }

    @Test
    fun `useSpan gives a span of the range's size`() {
        val sizes = listOf(
            ByteArray(7).useSpan(2, 5) { it.size },
            ShortArray(7).useSpan(2, 5) { it.size },
            IntArray(7).useSpan(2, 5) { it.size },
            LongArray(7).useSpan(2, 5) { it.size },
            FloatArray(7).useSpan(2, 5) { it.size },
            DoubleArray(7).useSpan(2, 5) { it.size },
        )

        assertEquals(listOf(3, 6, 12, 24, 12, 24), sizes)
    }

    @Test
    fun `useSpan on an empty range gives an empty span`() {
        val sizes = listOf(
            ByteArray(0).useSpan { it.size },
            ShortArray(3).useSpan(3, 3) { it.size },
            IntArray(3).useSpan(1, 1) { it.size },
            LongArray(0).useSpan { it.size },
            FloatArray(2).useSpan(startIndex = 2) { it.size },
            DoubleArray(0).useSpan { it.size },
        )

        assertEquals(List(6) { 0 }, sizes)
    }

    @Test
    fun `useSpan spans can be copied into writable memory`() = withTestMemory(12) { memory ->
        val floats = floatArrayOf(0.5f, 1.5f, 2.5f)

        floats.useSpan { it.copyTo(memory) }

        assertEquals(listOf(0.5f, 1.5f, 2.5f), List(3) { memory.getFloat(it * 4) })
    }

    @Test
    fun `useSpan outside the array throws`() {
        assertFailsWith<IndexOutOfBoundsException> { ByteArray(4).useSpan(-1, 2) { } }
        assertFailsWith<IndexOutOfBoundsException> { ShortArray(4).useSpan(0, 5) { } }
        assertFailsWith<IndexOutOfBoundsException> { IntArray(4).useSpan(startIndex = 5, endIndex = 5) { } }
        assertFailsWith<IndexOutOfBoundsException> { LongArray(4).useSpan(endIndex = 5) { } }
        assertFailsWith<IllegalArgumentException> { FloatArray(4).useSpan(3, 2) { } }
        assertFailsWith<IllegalArgumentException> { DoubleArray(4).useSpan(4, 0) { } }
    }
}
