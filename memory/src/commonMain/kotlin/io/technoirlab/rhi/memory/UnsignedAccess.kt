@file:OptIn(ExperimentalContracts::class)

package io.technoirlab.rhi.memory

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns the byte at [offset] as a [UByte].
 */
inline fun MemorySpan.getUByte(offset: Int): UByte = getByte(offset).toUByte()

/**
 * Returns the little-endian [UShort] at [offset], which needs no alignment.
 */
inline fun MemorySpan.getUShort(offset: Int): UShort = getShort(offset).toUShort()

/**
 * Returns the little-endian [UInt] at [offset], which needs no alignment.
 */
inline fun MemorySpan.getUInt(offset: Int): UInt = getInt(offset).toUInt()

/**
 * Returns the little-endian [ULong] at [offset], which needs no alignment.
 */
inline fun MemorySpan.getULong(offset: Int): ULong = getLong(offset).toULong()

/**
 * Writes [value] at [offset].
 */
inline fun MutableMemorySpan.setUByte(offset: Int, value: UByte) = setByte(offset, value.toByte())

/**
 * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
 */
inline fun MutableMemorySpan.setUShort(offset: Int, value: UShort) = setShort(offset, value.toShort())

/**
 * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
 */
inline fun MutableMemorySpan.setUInt(offset: Int, value: UInt) = setInt(offset, value.toInt())

/**
 * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
 */
inline fun MutableMemorySpan.setULong(offset: Int, value: ULong) = setLong(offset, value.toLong())

/**
 * Copies bytes starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex] (exclusive).
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MemorySpan.read(destination: UByteArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size) =
    read(destination.asByteArray(), offset, startIndex, endIndex)

/**
 * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
 * (exclusive).
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MemorySpan.read(destination: UShortArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size) =
    read(destination.asShortArray(), offset, startIndex, endIndex)

/**
 * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
 * (exclusive).
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MemorySpan.read(destination: UIntArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size) =
    read(destination.asIntArray(), offset, startIndex, endIndex)

/**
 * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
 * (exclusive).
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MemorySpan.read(destination: ULongArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size) =
    read(destination.asLongArray(), offset, startIndex, endIndex)

/**
 * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span, starting at byte [offset].
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MutableMemorySpan.write(source: UByteArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size) =
    write(source.asByteArray(), offset, startIndex, endIndex)

/**
 * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
 * starting at byte [offset].
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MutableMemorySpan.write(source: UShortArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size) =
    write(source.asShortArray(), offset, startIndex, endIndex)

/**
 * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
 * starting at byte [offset].
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MutableMemorySpan.write(source: UIntArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size) =
    write(source.asIntArray(), offset, startIndex, endIndex)

/**
 * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
 * starting at byte [offset].
 *
 * @throws IndexOutOfBoundsException when the range leaves the span or the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
 */
@ExperimentalUnsignedTypes
inline fun MutableMemorySpan.write(source: ULongArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size) =
    write(source.asLongArray(), offset, startIndex, endIndex)

/**
 * Calls [block] with a read-only span over the bytes of this array from [startIndex] (inclusive) to [endIndex]
 * (exclusive), and returns its result.
 *
 * The span is valid only inside [block], which is called once and in place. It may be a copy of the array on any target,
 * and is one on Wasm. Do not change the array while [block] runs: whether the span shows the change depends on the target.
 *
 * @throws IndexOutOfBoundsException when the range leaves the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex], or the range holds more bytes than a span
 * can, which is [Int.MAX_VALUE].
 */
@ExperimentalUnsignedTypes
inline fun <R> UByteArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return asByteArray().useSpan(startIndex, endIndex, block)
}

/**
 * Calls [block] with a read-only span over the values of this array from [startIndex] (inclusive) to [endIndex]
 * (exclusive), in little-endian order, and returns its result.
 *
 * The span is valid only inside [block], which is called once and in place. It may be a copy of the array on any target,
 * and is one on Wasm. Do not change the array while [block] runs: whether the span shows the change depends on the target.
 *
 * @throws IndexOutOfBoundsException when the range leaves the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex], or the range holds more bytes than a span
 * can, which is [Int.MAX_VALUE].
 */
@ExperimentalUnsignedTypes
inline fun <R> UShortArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return asShortArray().useSpan(startIndex, endIndex, block)
}

/**
 * Calls [block] with a read-only span over the values of this array from [startIndex] (inclusive) to [endIndex]
 * (exclusive), in little-endian order, and returns its result.
 *
 * The span is valid only inside [block], which is called once and in place. It may be a copy of the array on any target,
 * and is one on Wasm. Do not change the array while [block] runs: whether the span shows the change depends on the target.
 *
 * @throws IndexOutOfBoundsException when the range leaves the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex], or the range holds more bytes than a span
 * can, which is [Int.MAX_VALUE].
 */
@ExperimentalUnsignedTypes
inline fun <R> UIntArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return asIntArray().useSpan(startIndex, endIndex, block)
}

/**
 * Calls [block] with a read-only span over the values of this array from [startIndex] (inclusive) to [endIndex]
 * (exclusive), in little-endian order, and returns its result.
 *
 * The span is valid only inside [block], which is called once and in place. It may be a copy of the array on any target,
 * and is one on Wasm. Do not change the array while [block] runs: whether the span shows the change depends on the target.
 *
 * @throws IndexOutOfBoundsException when the range leaves the array.
 * @throws IllegalArgumentException when [startIndex] is greater than [endIndex], or the range holds more bytes than a span
 * can, which is [Int.MAX_VALUE].
 */
@ExperimentalUnsignedTypes
inline fun <R> ULongArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return asLongArray().useSpan(startIndex, endIndex, block)
}
