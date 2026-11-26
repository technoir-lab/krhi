package io.technoirlab.rhi.memory

import io.technoirlab.rhi.memory.internal.addressable
import io.technoirlab.rhi.memory.internal.byteLengthOf
import io.technoirlab.rhi.memory.internal.byteOffsetOf
import io.technoirlab.rhi.memory.internal.bytesAfter
import io.technoirlab.rhi.memory.internal.checkArrayTransfer
import io.technoirlab.rhi.memory.internal.checkBufferRange
import io.technoirlab.rhi.memory.internal.checkSpanRange
import io.technoirlab.rhi.memory.internal.fillBytes
import io.technoirlab.rhi.memory.internal.throwElementOutOfBounds
import io.technoirlab.rhi.memory.internal.writeArray
import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView
import js.buffer.DataView

actual class MutableMemorySpan internal constructor(
    buffer: ArrayBuffer,
    byteOffset: Int,
    size: Int,
    view: DataView<ArrayBuffer>,
) : MemorySpan(buffer, byteOffset, size, view) {
    actual override fun slice(offset: Int, size: Int): MutableMemorySpan {
        checkSpanRange(offset, size, this.size)
        return MutableMemorySpan(buffer, byteOffset + offset, size, DataView(buffer, byteOffset + offset, size))
    }

    actual fun fill(value: Byte, offset: Int, size: Int) {
        checkSpanRange(offset, size, this.size)
        if (size != 0) {
            fillBytes(buffer, byteOffset + offset, size, value)
        }
    }

    actual fun setByte(offset: Int, value: Byte) = element(offset, Byte.SIZE_BYTES) { view.setInt8(offset, value) }

    actual fun setShort(offset: Int, value: Short) = element(offset, Short.SIZE_BYTES) { view.setInt16(offset, value, LITTLE_ENDIAN) }

    actual fun setInt(offset: Int, value: Int) = element(offset, Int.SIZE_BYTES) { view.setInt32(offset, value, LITTLE_ENDIAN) }

    actual fun setLong(offset: Int, value: Long) {
        // Two writes, so this one checks first: a failing second write must not leave the first one behind.
        if (isOutside(offset, Long.SIZE_BYTES)) throwElementOutOfBounds(offset, Long.SIZE_BYTES, size)
        view.setInt32(offset, value.toInt(), LITTLE_ENDIAN)
        view.setInt32(offset + Int.SIZE_BYTES, (value ushr Int.SIZE_BITS).toInt(), LITTLE_ENDIAN)
    }

    actual fun setFloat(offset: Int, value: Float) = element(offset, Float.SIZE_BYTES) { view.setFloat32(offset, value, LITTLE_ENDIAN) }

    actual fun setDouble(offset: Int, value: Double) = element(offset, Double.SIZE_BYTES) { view.setFloat64(offset, value, LITTLE_ENDIAN) }

    actual fun write(source: ByteArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, source.size, Byte.SIZE_BYTES, size) != 0) {
            writeArray(offset, source, startIndex, endIndex)
        }
    }

    actual fun write(source: ShortArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, source.size, Short.SIZE_BYTES, size) != 0) {
            writeArray(offset, source, startIndex, endIndex)
        }
    }

    actual fun write(source: IntArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, source.size, Int.SIZE_BYTES, size) != 0) {
            writeArray(offset, source, startIndex, endIndex)
        }
    }

    actual fun write(source: LongArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, source.size, Long.SIZE_BYTES, size) != 0) {
            writeArray(offset, source, startIndex, endIndex)
        }
    }

    actual fun write(source: FloatArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, source.size, Float.SIZE_BYTES, size) != 0) {
            writeArray(offset, source, startIndex, endIndex)
        }
    }

    actual fun write(source: DoubleArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, source.size, Double.SIZE_BYTES, size) != 0) {
            writeArray(offset, source, startIndex, endIndex)
        }
    }

    override fun toString(): String = describe("MutableMemorySpan")

    /**
     * Returns a read-only span over the same memory. Unlike this span seen as a [MemorySpan], it cannot be cast back to a
     * writable one.
     */
    internal fun asReadOnly(): MemorySpan = MemorySpan(buffer, byteOffset, size, view)
}

/**
 * Returns a writable span over [byteLength] bytes of [buffer], starting at [byteOffset]. Nothing is copied. By default the
 * bytes from [byteOffset] to the end of the buffer.
 *
 * A span addresses its buffer with an [Int], so it lies within the first [Int.MAX_VALUE] bytes of the buffer. A larger buffer
 * is seen through several spans only as far as that.
 *
 * @throws IndexOutOfBoundsException when the range leaves the buffer.
 * @throws IllegalArgumentException when the range ends further into the buffer than a span can address.
 */
fun MutableMemorySpan(
    buffer: ArrayBuffer,
    byteOffset: Int = 0,
    byteLength: Int = bytesAfter(byteLengthOf(buffer), byteOffset),
): MutableMemorySpan {
    checkBufferRange(byteLengthOf(buffer), byteOffset, byteLength)
    return MutableMemorySpan(buffer, byteOffset, byteLength, DataView(buffer, byteOffset, byteLength))
}

/**
 * Returns a writable span over the bytes of [view], such as a typed array. Nothing is copied.
 *
 * @throws IllegalArgumentException when the view ends further into its buffer than a span can address, which is
 * [Int.MAX_VALUE] bytes.
 */
fun MutableMemorySpan(view: ArrayBufferView<ArrayBuffer>): MutableMemorySpan =
    MutableMemorySpan(view.buffer, addressable(byteOffsetOf(view), "The offset of the view"), addressable(byteLengthOf(view), "The view"))
