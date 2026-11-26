package io.technoirlab.rhi.memory

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class UnsignedAccessTest {
    @Test
    fun `unsigned values above the signed range read back`() = withTestMemory(15) { span ->
        span.setUByte(0, 0xFEu)
        span.setUShort(1, 0xFEDCu)
        span.setUInt(3, 0xFEDCBA98u)
        span.setULong(7, 0xFEDCBA9876543210uL)

        val values = listOf(span.getUByte(0), span.getUShort(1), span.getUInt(3), span.getULong(7))

        assertEquals(listOf<Any>(0xFEu.toUByte(), 0xFEDCu.toUShort(), 0xFEDCBA98u, 0xFEDCBA9876543210uL), values)
    }

    @Test
    fun `unsigned values are little-endian`() = withTestMemory(4) { span ->
        span.setUInt(0, 0x04030201u)

        val bytes = span.toByteArray()

        assertContentEquals(byteSequence(4), bytes)
    }

    @Test
    @OptIn(ExperimentalUnsignedTypes::class)
    fun `unsigned arrays transfer through their signed arrays`() = withTestMemory(64) { span ->
        val bytes = ubyteArrayOf(0xFFu, 0x80u, 0x7Fu)
        val shorts = ushortArrayOf(0xFFFFu, 0x8000u)
        val ints = uintArrayOf(0xFFFFFFFFu, 0x80000000u)
        val longs = ulongArrayOf(ULong.MAX_VALUE, 0x8000000000000000uL)

        span.write(bytes, startIndex = 1)
        span.write(shorts, offset = 2)
        span.write(ints, offset = 6)
        span.write(longs, offset = 14)
        val readBytes = UByteArray(2).also { span.read(it) }
        val readShorts = UShortArray(2).also { span.read(it, offset = 2) }
        val readInts = UIntArray(2).also { span.read(it, offset = 6) }
        val readLongs = ULongArray(3).also { span.read(it, offset = 14, startIndex = 1) }

        assertContentEquals(ubyteArrayOf(0x80u, 0x7Fu), readBytes)
        assertContentEquals(shorts, readShorts)
        assertContentEquals(ints, readInts)
        assertContentEquals(ulongArrayOf(0uL, ULong.MAX_VALUE, 0x8000000000000000uL), readLongs)
        assertEquals(0x80000000u, span.getUInt(10))
    }

    @Test
    @OptIn(ExperimentalUnsignedTypes::class)
    fun `useSpan gives the bytes of each unsigned array type in little-endian order`() {
        val bytes = ubyteArrayOf(1u, 2u)
        val shorts = ushortArrayOf(0x0403u, 0x0605u)
        val ints = uintArrayOf(0x0A090807u)
        val longs = ulongArrayOf(0x1211100F0E0D0C0BuL)

        val result = listOf(
            bytes.useSpan { it.toByteArray() },
            shorts.useSpan { it.toByteArray() },
            ints.useSpan { it.toByteArray() },
            longs.useSpan { it.toByteArray() },
        ).reduce { all, next -> all + next }

        assertContentEquals(byteSequence(18), result)
    }

    @Test
    @OptIn(ExperimentalUnsignedTypes::class)
    fun `useSpan covers a range of an unsigned array`() {
        val shorts = ushortArrayOf(0xFFFFu, 0x8000u, 0x0001u)

        val values = shorts.useSpan(startIndex = 1) { span -> List(span.size / 2) { span.getUShort(it * 2) } }

        assertEquals(listOf<UShort>(0x8000u, 0x0001u), values)
    }

    @Test
    @OptIn(ExperimentalUnsignedTypes::class)
    fun `useSpan on an unsigned array calls its block in place so that the block can initialise a value`() {
        val byte: UByte
        val short: UShort
        val int: UInt
        val long: ULong

        ubyteArrayOf(0xFEu).useSpan { byte = it.getUByte(0) }
        ushortArrayOf(0xFEDCu).useSpan { short = it.getUShort(0) }
        uintArrayOf(0xFEDCBA98u).useSpan { int = it.getUInt(0) }
        ulongArrayOf(0xFEDCBA9876543210uL).useSpan { long = it.getULong(0) }

        val values = listOf<Any>(byte, short, int, long)
        assertEquals(listOf<Any>(0xFEu.toUByte(), 0xFEDCu.toUShort(), 0xFEDCBA98u, 0xFEDCBA9876543210uL), values)
    }
}
