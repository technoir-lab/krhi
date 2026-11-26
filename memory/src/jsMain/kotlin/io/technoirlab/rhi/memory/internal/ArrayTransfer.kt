package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan
import io.technoirlab.rhi.memory.MutableMemorySpan
import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView

// On JS a Kotlin array is a typed array, so its bytes move in one copy. A LongArray is one only when Long is a
// BigInt; otherwise its elements are objects and move one at a time.

/**
 * Whether the engine keeps the elements of a typed array in little-endian order. A typed array has the byte order of the host,
 * and a span is little-endian by contract, so only on such a host are the bytes of a Kotlin array the bytes of a span.
 */
private val hostIsLittleEndian = js("new Uint8Array(new Uint16Array([1]).buffer)[0] === 1").unsafeCast<Boolean>()

internal actual fun MemorySpan.readArray(offset: Int, destination: ByteArray, startIndex: Int, endIndex: Int) =
    readView(offset, destination.asTypedArray(), startIndex, endIndex, Byte.SIZE_BYTES)

internal actual fun MemorySpan.readArray(offset: Int, destination: ShortArray, startIndex: Int, endIndex: Int) =
    readView(offset, destination.asTypedArray(), startIndex, endIndex, Short.SIZE_BYTES)

internal actual fun MemorySpan.readArray(offset: Int, destination: IntArray, startIndex: Int, endIndex: Int) =
    readView(offset, destination.asTypedArray(), startIndex, endIndex, Int.SIZE_BYTES)

internal actual fun MemorySpan.readArray(offset: Int, destination: LongArray, startIndex: Int, endIndex: Int) {
    val view = destination.asTypedArrayOrNull()
    if (view != null) {
        readView(offset, view, startIndex, endIndex, Long.SIZE_BYTES)
    } else {
        for (index in startIndex until endIndex) {
            destination[index] = getLong(offset + (index - startIndex) * Long.SIZE_BYTES)
        }
    }
}

internal actual fun MemorySpan.readArray(offset: Int, destination: FloatArray, startIndex: Int, endIndex: Int) =
    readView(offset, destination.asTypedArray(), startIndex, endIndex, Float.SIZE_BYTES)

internal actual fun MemorySpan.readArray(offset: Int, destination: DoubleArray, startIndex: Int, endIndex: Int) =
    readView(offset, destination.asTypedArray(), startIndex, endIndex, Double.SIZE_BYTES)

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: ByteArray, startIndex: Int, endIndex: Int) =
    writeView(offset, source.asTypedArray(), startIndex, endIndex, Byte.SIZE_BYTES)

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: ShortArray, startIndex: Int, endIndex: Int) =
    writeView(offset, source.asTypedArray(), startIndex, endIndex, Short.SIZE_BYTES)

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: IntArray, startIndex: Int, endIndex: Int) =
    writeView(offset, source.asTypedArray(), startIndex, endIndex, Int.SIZE_BYTES)

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: LongArray, startIndex: Int, endIndex: Int) {
    val view = source.asTypedArrayOrNull()
    if (view != null) {
        writeView(offset, view, startIndex, endIndex, Long.SIZE_BYTES)
    } else {
        for (index in startIndex until endIndex) {
            setLong(offset + (index - startIndex) * Long.SIZE_BYTES, source[index])
        }
    }
}

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: FloatArray, startIndex: Int, endIndex: Int) =
    writeView(offset, source.asTypedArray(), startIndex, endIndex, Float.SIZE_BYTES)

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: DoubleArray, startIndex: Int, endIndex: Int) =
    writeView(offset, source.asTypedArray(), startIndex, endIndex, Double.SIZE_BYTES)

// The typed array that each of these Kotlin arrays is on JS.

internal fun ByteArray.asTypedArray(): ArrayBufferView<ArrayBuffer> = unsafeCast<ArrayBufferView<ArrayBuffer>>()

internal fun ShortArray.asTypedArray(): ArrayBufferView<ArrayBuffer> = unsafeCast<ArrayBufferView<ArrayBuffer>>()

internal fun IntArray.asTypedArray(): ArrayBufferView<ArrayBuffer> = unsafeCast<ArrayBufferView<ArrayBuffer>>()

internal fun FloatArray.asTypedArray(): ArrayBufferView<ArrayBuffer> = unsafeCast<ArrayBufferView<ArrayBuffer>>()

internal fun DoubleArray.asTypedArray(): ArrayBufferView<ArrayBuffer> = unsafeCast<ArrayBufferView<ArrayBuffer>>()

/**
 * Returns the `BigInt64Array` that this array is when the build represents a [Long] as a `BigInt`, and `null` when its
 * elements are objects.
 */
internal fun LongArray.asTypedArrayOrNull(): ArrayBufferView<ArrayBuffer>? =
    if (js("ArrayBuffer").isView(this).unsafeCast<Boolean>()) unsafeCast<ArrayBufferView<ArrayBuffer>>() else null

/**
 * Refuses to treat the bytes of a typed array of multi-byte elements as the bytes of a span on a big-endian host.
 */
internal fun checkByteOrder(elementSize: Int) {
    if (elementSize != Byte.SIZE_BYTES && !hostIsLittleEndian) {
        throw UnsupportedOperationException("An array of multi-byte values can stand for a span only on a little-endian host")
    }
}

private fun MemorySpan.readView(offset: Int, destination: ArrayBufferView<ArrayBuffer>, startIndex: Int, endIndex: Int, elementSize: Int) {
    checkByteOrder(elementSize)
    val destinationOffset = arrayOffset(destination, startIndex, elementSize)
    copyArrayBytes(destination.buffer, destinationOffset, buffer, (byteOffset + offset).toDouble(), (endIndex - startIndex) * elementSize)
}

private fun MutableMemorySpan.writeView(
    offset: Int,
    source: ArrayBufferView<ArrayBuffer>,
    startIndex: Int,
    endIndex: Int,
    elementSize: Int,
) {
    checkByteOrder(elementSize)
    val sourceOffset = arrayOffset(source, startIndex, elementSize)
    copyArrayBytes(buffer, (byteOffset + offset).toDouble(), source.buffer, sourceOffset, (endIndex - startIndex) * elementSize)
}

// An array can be longer than 2 GiB, so the offset of an element in the bytes of its array is a Double: as an Int it would
// wrap, and the bytes would move to or from another place in the array.

private fun arrayOffset(array: ArrayBufferView<ArrayBuffer>, index: Int, elementSize: Int): Double =
    byteOffsetOf(array) + index.toDouble() * elementSize

/**
 * Copies [byteLength] bytes, which may overlap, between two buffers, at offsets that may lie past 2 GiB.
 */
private fun copyArrayBytes(
    destination: ArrayBuffer,
    destinationOffset: Double,
    source: ArrayBuffer,
    sourceOffset: Double,
    byteLength: Int,
): Unit = js("new Uint8Array(destination, destinationOffset, byteLength).set(new Uint8Array(source, sourceOffset, byteLength))")
