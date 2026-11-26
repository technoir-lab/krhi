package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan
import io.technoirlab.rhi.memory.MutableMemorySpan
import js.buffer.ArrayBuffer

// A Kotlin array on Wasm is invisible to JS, so the span is a copy of the range in a temporary buffer.

@PublishedApi
internal actual fun arraySpan(array: ByteArray, startIndex: Int, endIndex: Int): MemorySpan =
    temporaryCopy(startIndex, endIndex, array.size, Byte.SIZE_BYTES) { write(array, startIndex = startIndex, endIndex = endIndex) }

@PublishedApi
internal actual fun arraySpan(array: ShortArray, startIndex: Int, endIndex: Int): MemorySpan =
    temporaryCopy(startIndex, endIndex, array.size, Short.SIZE_BYTES) { write(array, startIndex = startIndex, endIndex = endIndex) }

@PublishedApi
internal actual fun arraySpan(array: IntArray, startIndex: Int, endIndex: Int): MemorySpan =
    temporaryCopy(startIndex, endIndex, array.size, Int.SIZE_BYTES) { write(array, startIndex = startIndex, endIndex = endIndex) }

@PublishedApi
internal actual fun arraySpan(array: LongArray, startIndex: Int, endIndex: Int): MemorySpan =
    temporaryCopy(startIndex, endIndex, array.size, Long.SIZE_BYTES) { write(array, startIndex = startIndex, endIndex = endIndex) }

@PublishedApi
internal actual fun arraySpan(array: FloatArray, startIndex: Int, endIndex: Int): MemorySpan =
    temporaryCopy(startIndex, endIndex, array.size, Float.SIZE_BYTES) { write(array, startIndex = startIndex, endIndex = endIndex) }

@PublishedApi
internal actual fun arraySpan(array: DoubleArray, startIndex: Int, endIndex: Int): MemorySpan =
    temporaryCopy(startIndex, endIndex, array.size, Double.SIZE_BYTES) { write(array, startIndex = startIndex, endIndex = endIndex) }

/**
 * Returns a read-only span over a temporary buffer for the array range, which [fill] writes into it. The span is read-only
 * as an object and not only by its type, so that a caller cannot cast it and write to a copy that is then thrown away.
 */
private inline fun temporaryCopy(
    startIndex: Int,
    endIndex: Int,
    arraySize: Int,
    elementSize: Int,
    fill: MutableMemorySpan.() -> Unit,
): MemorySpan {
    val copy = MutableMemorySpan(ArrayBuffer(checkArraySpan(startIndex, endIndex, arraySize, elementSize)))
    copy.fill()
    return copy.asReadOnly()
}
