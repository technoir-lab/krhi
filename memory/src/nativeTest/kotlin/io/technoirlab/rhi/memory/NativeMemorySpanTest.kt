@file:OptIn(ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.rawValue
import kotlinx.cinterop.set
import kotlinx.cinterop.toCPointer
import kotlinx.cinterop.value
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class NativeMemorySpanTest {
    @Test
    fun `span from a pointer reads the memory at it`() {
        memScoped {
            val value = alloc<IntVar>()
            value.value = 0x04030201

            val span = MemorySpan(value.ptr, 4)

            assertEquals(0x04030201, span.getInt(0))
            assertContentEquals(byteSequence(4), span.toByteArray())
        }
    }

    @Test
    fun `mutable span from a pointer writes the memory at it`() {
        memScoped {
            val value = alloc<IntVar>()

            MutableMemorySpan(value.ptr, 4).setInt(0, 0x04030201)

            assertEquals(0x04030201, value.value)
        }
    }

    @Test
    fun `pointer is the address of the first byte`() {
        memScoped {
            val bytes = allocArray<ByteVar>(8)
            bytes[5] = 42

            val span = MemorySpan(bytes, 8)
            val slice = span.slice(5)

            assertEquals(bytes, span.pointer)
            assertEquals(bytes + 5, slice.pointer)
            assertEquals(42, slice.getByte(0))
        }
    }

    @Test
    fun `zero-length span over a null pointer touches no memory`() = withTestMemory(4) { memory ->
        memory.write(byteSequence(4))
        val span = MutableMemorySpan(null, 0)

        span.copyTo(memory, destinationOffset = 4)
        memory.copyTo(span, offset = 4)
        span.fill(1)
        span.read(ByteArray(0))
        span.write(IntArray(2), startIndex = 2)
        val slice = span.slice(0, 0)

        assertNull(span.pointer)
        assertNull(slice.pointer)
        assertContentEquals(byteSequence(4), memory.toByteArray())
    }

    @Test
    fun `span over a null pointer must be empty`() {
        assertFailsWith<IllegalArgumentException> { MemorySpan(null, 1) }
        assertFailsWith<IllegalArgumentException> { MutableMemorySpan(null, 1) }
    }

    @Test
    fun `span size must not be negative`() = withTestMemory(4) { memory ->
        val pointer = memory.pointer

        assertFailsWith<IllegalArgumentException> { MemorySpan(pointer, -1) }
        assertFailsWith<IllegalArgumentException> { MutableMemorySpan(pointer, -1) }
    }

    @Test
    fun `span size must not be more than a span can hold`() = withTestMemory(4) { memory ->
        val pointer = memory.pointer

        val largest = MemorySpan(pointer, Int.MAX_VALUE.toLong())

        assertEquals(Int.MAX_VALUE, largest.size)
        assertFailsWith<IllegalArgumentException> { MemorySpan(pointer, Int.MAX_VALUE + 1L) }
        assertFailsWith<IllegalArgumentException> { MutableMemorySpan(pointer, 1L shl 32) }
    }

    @Test
    fun `toString shows the address of the span`() {
        memScoped {
            val bytes = allocArray<ByteVar>(8)

            val text = MemorySpan(bytes + 3, 5).toString()

            assertEquals("MemorySpan(address=0x${(bytes + 3).rawValue.toLong().toString(16)}, size=5)", text)
        }
    }

    @Test
    fun `toString shows an address with its top bit set as unsigned`() {
        val pointer = (Long.MIN_VALUE or 0x1000).toCPointer<ByteVar>()

        val text = MemorySpan(pointer, 0).toString()

        assertEquals("MemorySpan(address=0x8000000000001000, size=0)", text)
    }
}
