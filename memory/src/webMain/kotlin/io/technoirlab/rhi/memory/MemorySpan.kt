package io.technoirlab.rhi.memory

import io.technoirlab.rhi.memory.internal.addressable
import io.technoirlab.rhi.memory.internal.byteLengthOf
import io.technoirlab.rhi.memory.internal.byteOffsetOf
import io.technoirlab.rhi.memory.internal.bytesAfter
import io.technoirlab.rhi.memory.internal.checkArrayTransfer
import io.technoirlab.rhi.memory.internal.checkBufferRange
import io.technoirlab.rhi.memory.internal.checkSpanRange
import io.technoirlab.rhi.memory.internal.copyBytes
import io.technoirlab.rhi.memory.internal.readArray
import io.technoirlab.rhi.memory.internal.throwElementOutOfBounds
import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView
import js.buffer.DataView
import js.typedarrays.Uint8Array

// The span keeps its buffer, offset and length as Kotlin fields: on Wasm every read of a DataView property is a call into JS.
actual open class MemorySpan internal constructor(
    internal val buffer: ArrayBuffer,
    internal val byteOffset: Int,
    actual val size: Int,
    // A parameter, so that a read-only span over the memory of a writable one shares its view and makes no call into JS.
    internal val view: DataView<ArrayBuffer>,
) {
    /**
     * Returns a view of the bytes of the span, to pass straight to a JS API.
     *
     * Do not keep the view: it is valid only as long as the span is.
     *
     * JS has no read-only view, so this one can be written through even when the span is read-only. Hand it to an API that
     * writes to its argument only when the span is a [MutableMemorySpan]. A read-only span may be a copy of the data it
     * stands for, as the span of `useSpan` is on Wasm and is not on JS, so such a write is lost on one target and changes
     * the caller's data on the other.
     */
    fun asUint8Array(): Uint8Array<ArrayBuffer> = Uint8Array(buffer, byteOffset, size)

    actual open fun slice(offset: Int, size: Int): MemorySpan {
        checkSpanRange(offset, size, this.size)
        return MemorySpan(buffer, byteOffset + offset, size, DataView(buffer, byteOffset + offset, size))
    }

    actual fun copyTo(destination: MutableMemorySpan, destinationOffset: Int, offset: Int, size: Int) {
        checkSpanRange(offset, size, this.size, "the source span")
        checkSpanRange(destinationOffset, size, destination.size, "the destination span")
        if (size != 0) {
            copyBytes(destination.buffer, destination.byteOffset + destinationOffset, buffer, byteOffset + offset, size)
        }
    }

    actual fun getByte(offset: Int): Byte = element(offset, Byte.SIZE_BYTES) { view.getInt8(offset) }

    actual fun getShort(offset: Int): Short = element(offset, Short.SIZE_BYTES) { view.getInt16(offset, LITTLE_ENDIAN) }

    actual fun getInt(offset: Int): Int = element(offset, Int.SIZE_BYTES) { view.getInt32(offset, LITTLE_ENDIAN) }

    actual fun getLong(offset: Int): Long = element(offset, Long.SIZE_BYTES) {
        val low = view.getInt32(offset, LITTLE_ENDIAN)
        val high = view.getInt32(offset + Int.SIZE_BYTES, LITTLE_ENDIAN)
        (high.toLong() shl Int.SIZE_BITS) or (low.toLong() and 0xFFFFFFFFL)
    }

    actual fun getFloat(offset: Int): Float = element(offset, Float.SIZE_BYTES) { view.getFloat32(offset, LITTLE_ENDIAN) }

    actual fun getDouble(offset: Int): Double = element(offset, Double.SIZE_BYTES) { view.getFloat64(offset, LITTLE_ENDIAN) }

    actual fun read(destination: ByteArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, destination.size, Byte.SIZE_BYTES, size) != 0) {
            readArray(offset, destination, startIndex, endIndex)
        }
    }

    actual fun read(destination: ShortArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, destination.size, Short.SIZE_BYTES, size) != 0) {
            readArray(offset, destination, startIndex, endIndex)
        }
    }

    actual fun read(destination: IntArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, destination.size, Int.SIZE_BYTES, size) != 0) {
            readArray(offset, destination, startIndex, endIndex)
        }
    }

    actual fun read(destination: LongArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, destination.size, Long.SIZE_BYTES, size) != 0) {
            readArray(offset, destination, startIndex, endIndex)
        }
    }

    actual fun read(destination: FloatArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, destination.size, Float.SIZE_BYTES, size) != 0) {
            readArray(offset, destination, startIndex, endIndex)
        }
    }

    actual fun read(destination: DoubleArray, offset: Int, startIndex: Int, endIndex: Int) {
        if (checkArrayTransfer(offset, startIndex, endIndex, destination.size, Double.SIZE_BYTES, size) != 0) {
            readArray(offset, destination, startIndex, endIndex)
        }
    }

    override fun toString(): String = describe("MemorySpan")

    // The platform checks every access to its DataView. Its error becomes the exception that every target throws for an element
    // outside the span, so that an access pays for no second check of its own.
    internal inline fun <T> element(offset: Int, elementSize: Int, access: () -> T): T = try {
        access()
    } catch (e: Throwable) {
        throw elementFailure(e, offset, elementSize)
    }

    internal fun isOutside(offset: Int, elementSize: Int): Boolean = offset < 0 || offset > size - elementSize

    // Returns the platform's error when it has another cause than the range, such as a detached buffer.
    internal fun elementFailure(cause: Throwable, offset: Int, elementSize: Int): Throwable {
        if (isOutside(offset, elementSize)) throwElementOutOfBounds(offset, elementSize, size)
        return cause
    }

    internal fun describe(typeName: String): String = "$typeName(byteOffset=$byteOffset, size=$size)"

    internal companion object {
        /**
         * Passed to every [DataView] call, which is big-endian unless told otherwise.
         */
        const val LITTLE_ENDIAN = true
    }
}

/**
 * Returns a read-only span over [byteLength] bytes of [buffer], starting at [byteOffset]. Nothing is copied. By default the
 * bytes from [byteOffset] to the end of the buffer.
 *
 * A span addresses its buffer with an [Int], so it lies within the first [Int.MAX_VALUE] bytes of the buffer. A larger buffer
 * is seen through several spans only as far as that.
 *
 * @throws IndexOutOfBoundsException when the range leaves the buffer.
 * @throws IllegalArgumentException when the range ends further into the buffer than a span can address.
 */
fun MemorySpan(buffer: ArrayBuffer, byteOffset: Int = 0, byteLength: Int = bytesAfter(byteLengthOf(buffer), byteOffset)): MemorySpan {
    checkBufferRange(byteLengthOf(buffer), byteOffset, byteLength)
    return MemorySpan(buffer, byteOffset, byteLength, DataView(buffer, byteOffset, byteLength))
}

/**
 * Returns a read-only span over the bytes of [view], such as a typed array. Nothing is copied.
 *
 * @throws IllegalArgumentException when the view ends further into its buffer than a span can address, which is
 * [Int.MAX_VALUE] bytes.
 */
fun MemorySpan(view: ArrayBufferView<ArrayBuffer>): MemorySpan =
    MemorySpan(view.buffer, addressable(byteOffsetOf(view), "The offset of the view"), addressable(byteLengthOf(view), "The view"))
