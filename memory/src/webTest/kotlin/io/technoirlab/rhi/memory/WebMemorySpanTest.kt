package io.technoirlab.rhi.memory

import js.buffer.ArrayBuffer
import js.buffer.DataView
import js.typedarrays.Uint8Array
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class WebMemorySpanTest {
    @Test
    fun `span over a buffer reads its bytes`() {
        val buffer = ArrayBuffer(8)
        DataView(buffer).setInt32(4, 0x04030201, true)

        val span = MemorySpan(buffer)

        assertEquals(8, span.size)
        assertEquals(0x04030201, span.getInt(4))
    }

    @Test
    fun `span over part of a buffer starts at its offset`() {
        val buffer = ArrayBuffer(16)
        MutableMemorySpan(buffer).write(byteSequence(16))

        val span = MemorySpan(buffer, byteOffset = 3, byteLength = 5)

        assertContentEquals(byteSequence(5, first = 4), span.toByteArray())
    }

    @Test
    fun `span over a typed array covers the bytes of its view`() {
        val buffer = ArrayBuffer(16)
        val view = Uint8Array(buffer, 4, 8)

        MutableMemorySpan(view).fill(9)

        assertContentEquals(byteArrayOf(0, 0, 0, 0, 9, 9, 9, 9, 9, 9, 9, 9, 0, 0, 0, 0), MemorySpan(buffer).toByteArray())
    }

    @Test
    @OptIn(ExperimentalWasmJsInterop::class)
    fun `asUint8Array views the bytes of the span`() {
        val span = MutableMemorySpan(ArrayBuffer(8)).slice(2, 4)
        span.setInt(0, 0x04030201)
        val source = MutableMemorySpan(ArrayBuffer(1))
        source.setByte(0, 9)

        val view = span.asUint8Array()
        view.set(source.asUint8Array(), 3)

        assertEquals(4, view.byteLength)
        assertContentEquals(byteArrayOf(1, 2, 3, 9), span.toByteArray())
    }

    @Test
    fun `span outside its buffer throws`() {
        val buffer = ArrayBuffer(8)

        assertFailsWith<IndexOutOfBoundsException> { MemorySpan(buffer, byteOffset = 9) }
        assertFailsWith<IndexOutOfBoundsException> { MemorySpan(buffer, byteOffset = 4, byteLength = 5) }
        assertFailsWith<IndexOutOfBoundsException> { MutableMemorySpan(buffer, byteOffset = -1) }
    }

    @Test
    fun `offset outside the buffer is reported without the length it gave by default`() {
        val buffer = ArrayBuffer(8)

        val failure = assertFailsWith<IndexOutOfBoundsException> { MemorySpan(buffer, byteOffset = 10) }

        assertEquals("Offset 10 is outside a buffer of 8 bytes", failure.message)
    }

    @Test
    fun `toString shows the offset of the span in its buffer`() {
        val span = MemorySpan(ArrayBuffer(16), byteOffset = 3, byteLength = 5)

        val text = span.toString()

        assertEquals("MemorySpan(byteOffset=3, size=5)", text)
    }
}
