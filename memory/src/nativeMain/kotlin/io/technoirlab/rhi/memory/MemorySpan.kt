@file:OptIn(ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.rawValue
import kotlinx.cinterop.usePinned
import platform.posix.memmove

actual open class MemorySpan internal constructor(
    internal val address: NativePtr,
    actual val size: Int,
) {
    /**
     * The address of the first byte. It is null only for an empty span over no memory at all, which is what a null pointer
     * makes, and what `useSpan` gives for an empty range of an array.
     *
     * Do not keep the pointer: it is valid only as long as the span is, and the span of `useSpan` only inside its block.
     *
     * A pointer has no read-only form, so this one can be written through even when the span is read-only. Hand it to a
     * function that writes to its argument only when the span is a [MutableMemorySpan]. A read-only span stands for data
     * that its owner does not expect to change: through the span of `useSpan`, such a write changes the caller's array.
     */
    val pointer: CPointer<ByteVar>?
        get() = interpretCPointer(address)

    actual open fun slice(offset: Int, size: Int): MemorySpan {
        checkSpanRange(offset, size, this.size)
        return MemorySpan(address + offset.toLong(), size)
    }

    actual fun copyTo(destination: MutableMemorySpan, destinationOffset: Int, offset: Int, size: Int) {
        checkSpanRange(offset, size, this.size, "the source span")
        checkSpanRange(destinationOffset, size, destination.size, "the destination span")
        if (size != 0) {
            moveBytes(destination.pointerAt(destinationOffset), pointerAt(offset), size)
        }
    }

    actual fun getByte(offset: Int): Byte {
        checkElement(offset, Byte.SIZE_BYTES)
        return loadByte(address + offset.toLong())
    }

    actual fun getShort(offset: Int): Short {
        checkElement(offset, Short.SIZE_BYTES)
        return loadShort(address + offset.toLong())
    }

    actual fun getInt(offset: Int): Int {
        checkElement(offset, Int.SIZE_BYTES)
        return loadInt(address + offset.toLong())
    }

    actual fun getLong(offset: Int): Long {
        checkElement(offset, Long.SIZE_BYTES)
        return loadLong(address + offset.toLong())
    }

    actual fun getFloat(offset: Int): Float {
        checkElement(offset, Float.SIZE_BYTES)
        return Float.fromBits(loadInt(address + offset.toLong()))
    }

    actual fun getDouble(offset: Int): Double {
        checkElement(offset, Double.SIZE_BYTES)
        return Double.fromBits(loadLong(address + offset.toLong()))
    }

    actual fun read(destination: ByteArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, destination.size, Byte.SIZE_BYTES, size)
        if (byteCount != 0) {
            destination.usePinned { moveBytes(it.addressOf(startIndex), pointerAt(offset), byteCount) }
        }
    }

    actual fun read(destination: ShortArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, destination.size, Short.SIZE_BYTES, size)
        if (byteCount != 0) {
            destination.usePinned { moveBytes(it.addressOf(startIndex), pointerAt(offset), byteCount) }
        }
    }

    actual fun read(destination: IntArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, destination.size, Int.SIZE_BYTES, size)
        if (byteCount != 0) {
            destination.usePinned { moveBytes(it.addressOf(startIndex), pointerAt(offset), byteCount) }
        }
    }

    actual fun read(destination: LongArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, destination.size, Long.SIZE_BYTES, size)
        if (byteCount != 0) {
            destination.usePinned { moveBytes(it.addressOf(startIndex), pointerAt(offset), byteCount) }
        }
    }

    actual fun read(destination: FloatArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, destination.size, Float.SIZE_BYTES, size)
        if (byteCount != 0) {
            destination.usePinned { moveBytes(it.addressOf(startIndex), pointerAt(offset), byteCount) }
        }
    }

    actual fun read(destination: DoubleArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, destination.size, Double.SIZE_BYTES, size)
        if (byteCount != 0) {
            destination.usePinned { moveBytes(it.addressOf(startIndex), pointerAt(offset), byteCount) }
        }
    }

    override fun toString(): String = describe("MemorySpan")

    internal fun pointerAt(offset: Int): CPointer<ByteVar>? = interpretCPointer(address + offset.toLong())

    internal fun checkElement(offset: Int, elementSize: Int) {
        if (offset < 0 || offset > size - elementSize) throwElementOutOfBounds(offset, elementSize, size)
    }

    // The address is unsigned: one with its top bit set, such as a tagged pointer on Android, would print as 0x-… otherwise.
    internal fun describe(typeName: String): String = "$typeName(address=0x${address.toLong().toULong().toString(HEX_RADIX)}, size=$size)"

    private companion object {
        private const val HEX_RADIX = 16
    }
}

/**
 * Returns a read-only span over [size] bytes at [pointer]. The pointer may be null only when [size] is zero.
 *
 * The span does not own the memory: whoever allocated it keeps it valid for as long as the span is used.
 *
 * The size is a [Long], which is what native code counts bytes in, and is checked against what a span can hold, so that no
 * caller has to narrow it unchecked. Memory larger than that is seen through several spans.
 *
 * @throws IllegalArgumentException when [size] is negative or more than [Int.MAX_VALUE], or [pointer] is null and [size] is
 * not zero.
 */
fun MemorySpan(pointer: CPointer<*>?, size: Long): MemorySpan = MemorySpan(pointer.rawValue, checkPointer(pointer, size))

/**
 * Copies [byteCount] bytes, which may overlap, from [source] to [destination].
 */
internal fun moveBytes(destination: CPointer<*>?, source: CPointer<*>?, byteCount: Int) {
    memmove(destination, source, byteCount.convert())
}

/**
 * Checks the arguments of a span over [size] bytes at [pointer], and returns the size as the [Int] that a span keeps.
 */
internal fun checkPointer(pointer: CPointer<*>?, size: Long): Int {
    require(size >= 0) { "Size must not be negative: $size" }
    require(size <= Int.MAX_VALUE) { "A span holds at most ${Int.MAX_VALUE} bytes: $size" }
    require(pointer != null || size == 0L) { "Pointer must not be null for a span of $size bytes" }
    return size.toInt()
}
