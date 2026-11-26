package io.technoirlab.rhi.memory.internal

import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView
import kotlin.js.ExperimentalWasmJsInterop

// The platform counts bytes in JS numbers, which reach past an Int, and a buffer can be longer than 2 GiB. Read as an Int such
// a length wraps, and one of 4 GiB reads as zero. Lengths and offsets that the platform gives are therefore read as Double,
// and checked before they become the Int that a span keeps.

@OptIn(ExperimentalWasmJsInterop::class)
internal fun byteLengthOf(buffer: ArrayBuffer): Double = js("buffer.byteLength")

@OptIn(ExperimentalWasmJsInterop::class)
internal fun byteOffsetOf(view: ArrayBufferView<ArrayBuffer>): Double = js("view.byteOffset")

@OptIn(ExperimentalWasmJsInterop::class)
internal fun byteLengthOf(view: ArrayBufferView<ArrayBuffer>): Double = js("view.byteLength")

/**
 * Returns [bytes], an offset or a length that the platform gave and that [what] names, as an Int.
 *
 * @throws IllegalArgumentException when it is more than a span can address.
 */
internal fun addressable(bytes: Double, what: String): Int {
    require(bytes <= Int.MAX_VALUE) { "$what is ${bytes.toLong()} bytes, more than a span can address, which is ${Int.MAX_VALUE}" }
    return bytes.toInt()
}

/**
 * Returns the bytes from [byteOffset] to the end of a buffer of [bufferLength] bytes, for a span that is to cover them.
 */
internal fun bytesAfter(bufferLength: Double, byteOffset: Int): Int = addressable(bufferLength - byteOffset, "The rest of the buffer")

/**
 * Checks that [byteLength] bytes at [byteOffset] lie inside a buffer of [bufferLength] bytes, and where a span can address
 * them.
 */
internal fun checkBufferRange(bufferLength: Double, byteOffset: Int, byteLength: Int) {
    if (byteOffset < 0 || byteLength < 0 || byteOffset > bufferLength - byteLength) {
        throwRangeOutOfBounds(byteOffset.toLong(), byteLength.toLong(), bufferLength.toLong(), "a buffer")
    }
    // Offsets into the buffer are Int inside the span, so the last byte of the span has to have one.
    require(byteOffset <= Int.MAX_VALUE - byteLength) {
        "A range of $byteLength bytes at offset $byteOffset ends further into its buffer than a span can address"
    }
}

// Each bulk operation is one call into JS. Through the typed-array wrappers it would take several, and on Wasm every call
// costs tens of nanoseconds; the wrapper's fill also returns its array, which Wasm handles slowly.

/**
 * Copies [byteLength] bytes, which may overlap, between two buffers.
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal fun copyBytes(destination: ArrayBuffer, destinationOffset: Int, source: ArrayBuffer, sourceOffset: Int, byteLength: Int): Unit =
    js("new Uint8Array(destination, destinationOffset, byteLength).set(new Uint8Array(source, sourceOffset, byteLength))")

/**
 * Sets [byteLength] bytes of [buffer] starting at [byteOffset] to [value].
 */
@OptIn(ExperimentalWasmJsInterop::class)
internal fun fillBytes(buffer: ArrayBuffer, byteOffset: Int, byteLength: Int, value: Byte): Unit =
    js("new Int8Array(buffer, byteOffset, byteLength).fill(value)")
