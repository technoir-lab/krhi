@file:OptIn(ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.rawValue
import kotlinx.cinterop.usePinned
import platform.posix.memset

actual class MutableMemorySpan internal constructor(
    address: NativePtr,
    size: Int,
) : MemorySpan(address, size) {
    actual override fun slice(offset: Int, size: Int): MutableMemorySpan {
        checkSpanRange(offset, size, this.size)
        return MutableMemorySpan(address + offset.toLong(), size)
    }

    actual fun fill(value: Byte, offset: Int, size: Int) {
        checkSpanRange(offset, size, this.size)
        if (size != 0) {
            memset(pointerAt(offset), value.toInt(), size.convert())
        }
    }

    actual fun setByte(offset: Int, value: Byte) {
        checkElement(offset, Byte.SIZE_BYTES)
        storeByte(address + offset.toLong(), value)
    }

    actual fun setShort(offset: Int, value: Short) {
        checkElement(offset, Short.SIZE_BYTES)
        storeShort(address + offset.toLong(), value)
    }

    actual fun setInt(offset: Int, value: Int) {
        checkElement(offset, Int.SIZE_BYTES)
        storeInt(address + offset.toLong(), value)
    }

    actual fun setLong(offset: Int, value: Long) {
        checkElement(offset, Long.SIZE_BYTES)
        storeLong(address + offset.toLong(), value)
    }

    actual fun setFloat(offset: Int, value: Float) {
        checkElement(offset, Float.SIZE_BYTES)
        storeInt(address + offset.toLong(), value.toRawBits())
    }

    actual fun setDouble(offset: Int, value: Double) {
        checkElement(offset, Double.SIZE_BYTES)
        storeLong(address + offset.toLong(), value.toRawBits())
    }

    actual fun write(source: ByteArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, source.size, Byte.SIZE_BYTES, size)
        if (byteCount != 0) {
            source.usePinned { moveBytes(pointerAt(offset), it.addressOf(startIndex), byteCount) }
        }
    }

    actual fun write(source: ShortArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, source.size, Short.SIZE_BYTES, size)
        if (byteCount != 0) {
            source.usePinned { moveBytes(pointerAt(offset), it.addressOf(startIndex), byteCount) }
        }
    }

    actual fun write(source: IntArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, source.size, Int.SIZE_BYTES, size)
        if (byteCount != 0) {
            source.usePinned { moveBytes(pointerAt(offset), it.addressOf(startIndex), byteCount) }
        }
    }

    actual fun write(source: LongArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, source.size, Long.SIZE_BYTES, size)
        if (byteCount != 0) {
            source.usePinned { moveBytes(pointerAt(offset), it.addressOf(startIndex), byteCount) }
        }
    }

    actual fun write(source: FloatArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, source.size, Float.SIZE_BYTES, size)
        if (byteCount != 0) {
            source.usePinned { moveBytes(pointerAt(offset), it.addressOf(startIndex), byteCount) }
        }
    }

    actual fun write(source: DoubleArray, offset: Int, startIndex: Int, endIndex: Int) {
        val byteCount = checkArrayTransfer(offset, startIndex, endIndex, source.size, Double.SIZE_BYTES, size)
        if (byteCount != 0) {
            source.usePinned { moveBytes(pointerAt(offset), it.addressOf(startIndex), byteCount) }
        }
    }

    override fun toString(): String = describe("MutableMemorySpan")
}

/**
 * Returns a writable span over [size] bytes at [pointer]. The pointer may be null only when [size] is zero.
 *
 * The span does not own the memory: whoever allocated it keeps it valid for as long as the span is used.
 *
 * The size is a [Long], which is what native code counts bytes in, and is checked against what a span can hold, so that no
 * caller has to narrow it unchecked. Memory larger than that is seen through several spans.
 *
 * @throws IllegalArgumentException when [size] is negative or more than [Int.MAX_VALUE], or [pointer] is null and [size] is
 * not zero.
 */
fun MutableMemorySpan(pointer: CPointer<*>?, size: Long): MutableMemorySpan =
    MutableMemorySpan(pointer.rawValue, checkPointer(pointer, size))
