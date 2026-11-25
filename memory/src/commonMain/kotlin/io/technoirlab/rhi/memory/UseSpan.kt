@file:OptIn(ExperimentalContracts::class)

package io.technoirlab.rhi.memory

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// An expect function has no body to declare a contract in, and common code does not see a contract that only an actual
// declares. Each useSpan is therefore a common function that declares the contract and hands over to the platform's
// withArraySpan. Every actual of that declares the same contract, where the compiler checks it. It cannot see that through
// the expect declaration, which is the warning suppressed on each contract here.

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
inline fun <R> ByteArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    @Suppress("WRONG_INVOCATION_KIND")
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withArraySpan(this, startIndex, endIndex, block)
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
inline fun <R> ShortArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    @Suppress("WRONG_INVOCATION_KIND")
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withArraySpan(this, startIndex, endIndex, block)
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
inline fun <R> IntArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    @Suppress("WRONG_INVOCATION_KIND")
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withArraySpan(this, startIndex, endIndex, block)
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
inline fun <R> LongArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    @Suppress("WRONG_INVOCATION_KIND")
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withArraySpan(this, startIndex, endIndex, block)
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
inline fun <R> FloatArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    @Suppress("WRONG_INVOCATION_KIND")
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withArraySpan(this, startIndex, endIndex, block)
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
inline fun <R> DoubleArray.useSpan(startIndex: Int = 0, endIndex: Int = size, block: (MemorySpan) -> R): R {
    @Suppress("WRONG_INVOCATION_KIND")
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return withArraySpan(this, startIndex, endIndex, block)
}

/**
 * Calls [block] once with the span that `useSpan` gives for the range of [array], after checking the range.
 */
@PublishedApi
internal expect inline fun <R> withArraySpan(array: ByteArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R

/**
 * Calls [block] once with the span that `useSpan` gives for the range of [array], after checking the range.
 */
@PublishedApi
internal expect inline fun <R> withArraySpan(array: ShortArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R

/**
 * Calls [block] once with the span that `useSpan` gives for the range of [array], after checking the range.
 */
@PublishedApi
internal expect inline fun <R> withArraySpan(array: IntArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R

/**
 * Calls [block] once with the span that `useSpan` gives for the range of [array], after checking the range.
 */
@PublishedApi
internal expect inline fun <R> withArraySpan(array: LongArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R

/**
 * Calls [block] once with the span that `useSpan` gives for the range of [array], after checking the range.
 */
@PublishedApi
internal expect inline fun <R> withArraySpan(array: FloatArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R

/**
 * Calls [block] once with the span that `useSpan` gives for the range of [array], after checking the range.
 */
@PublishedApi
internal expect inline fun <R> withArraySpan(array: DoubleArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R
