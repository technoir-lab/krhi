package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan
import io.technoirlab.rhi.memory.MutableMemorySpan
import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView

// On JS a Kotlin array is a typed array, so the span is a view of it. A LongArray is one only when Long is a BigInt;
// otherwise its elements are objects, and the span is a copy.

@PublishedApi
internal actual fun arraySpan(array: ByteArray, startIndex: Int, endIndex: Int): MemorySpan =
    viewSpan(array.asTypedArray(), array.size, startIndex, endIndex, Byte.SIZE_BYTES)

@PublishedApi
internal actual fun arraySpan(array: ShortArray, startIndex: Int, endIndex: Int): MemorySpan =
    viewSpan(array.asTypedArray(), array.size, startIndex, endIndex, Short.SIZE_BYTES)

@PublishedApi
internal actual fun arraySpan(array: IntArray, startIndex: Int, endIndex: Int): MemorySpan =
    viewSpan(array.asTypedArray(), array.size, startIndex, endIndex, Int.SIZE_BYTES)

@PublishedApi
internal actual fun arraySpan(array: LongArray, startIndex: Int, endIndex: Int): MemorySpan {
    val view = array.asTypedArrayOrNull()
    if (view != null) return viewSpan(view, array.size, startIndex, endIndex, Long.SIZE_BYTES)
    val copy = MutableMemorySpan(ArrayBuffer(checkArraySpan(startIndex, endIndex, array.size, Long.SIZE_BYTES)))
    copy.write(array, startIndex = startIndex, endIndex = endIndex)
    // Read-only as an object and not only by its type, so that a caller cannot cast it and write to the copy.
    return copy.asReadOnly()
}

@PublishedApi
internal actual fun arraySpan(array: FloatArray, startIndex: Int, endIndex: Int): MemorySpan =
    viewSpan(array.asTypedArray(), array.size, startIndex, endIndex, Float.SIZE_BYTES)

@PublishedApi
internal actual fun arraySpan(array: DoubleArray, startIndex: Int, endIndex: Int): MemorySpan =
    viewSpan(array.asTypedArray(), array.size, startIndex, endIndex, Double.SIZE_BYTES)

/**
 * Returns a span over a range of the typed array behind a Kotlin array, without a copy.
 */
private fun viewSpan(view: ArrayBufferView<ArrayBuffer>, arraySize: Int, startIndex: Int, endIndex: Int, elementSize: Int): MemorySpan {
    val byteLength = checkArraySpan(startIndex, endIndex, arraySize, elementSize)
    checkByteOrder(elementSize)
    // An array can be longer than 2 GiB, where a span cannot start: its offset into the buffer of the array is an Int.
    val byteOffset = addressable(byteOffsetOf(view) + startIndex.toDouble() * elementSize, "The offset of the range in its array")
    return MemorySpan(view.buffer, byteOffset, byteLength)
}
