package io.technoirlab.rhi.memory

import js.buffer.ArrayBuffer
import js.buffer.ArrayBufferView
import js.buffer.DataView
import js.typedarrays.Uint8Array
import kotlin.js.ExperimentalWasmJsInterop

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

/**
 * Copies bytes from the span into an array range that the caller has checked and that is not empty.
 */
internal expect fun MemorySpan.readArray(offset: Int, destination: ByteArray, startIndex: Int, endIndex: Int)

/**
 * Copies little-endian values from the span into an array range that the caller has checked and that is not empty.
 */
internal expect fun MemorySpan.readArray(offset: Int, destination: ShortArray, startIndex: Int, endIndex: Int)

/**
 * Copies little-endian values from the span into an array range that the caller has checked and that is not empty.
 */
internal expect fun MemorySpan.readArray(offset: Int, destination: IntArray, startIndex: Int, endIndex: Int)

/**
 * Copies little-endian values from the span into an array range that the caller has checked and that is not empty.
 */
internal expect fun MemorySpan.readArray(offset: Int, destination: LongArray, startIndex: Int, endIndex: Int)

/**
 * Copies little-endian values from the span into an array range that the caller has checked and that is not empty.
 */
internal expect fun MemorySpan.readArray(offset: Int, destination: FloatArray, startIndex: Int, endIndex: Int)

/**
 * Copies little-endian values from the span into an array range that the caller has checked and that is not empty.
 */
internal expect fun MemorySpan.readArray(offset: Int, destination: DoubleArray, startIndex: Int, endIndex: Int)
