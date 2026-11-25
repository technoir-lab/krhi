@file:OptIn(ExperimentalContracts::class)

package io.technoirlab.rhi.memory

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal actual inline fun <R> withArraySpan(array: ByteArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: ShortArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: IntArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: LongArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: FloatArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: DoubleArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

// One function for each array type is all that the inline functions above expose to the code they are inlined into.

/**
 * Returns a read-only span over the bytes of [array] from [startIndex] (inclusive) to [endIndex] (exclusive): a view of the
 * array where JS can see it, and a copy otherwise.
 */
@PublishedApi
internal expect fun arraySpan(array: ByteArray, startIndex: Int, endIndex: Int): MemorySpan

/**
 * Returns a read-only span over the little-endian values of [array] from [startIndex] (inclusive) to [endIndex]
 * (exclusive): a view of the array where JS can see it, and a copy otherwise.
 */
@PublishedApi
internal expect fun arraySpan(array: ShortArray, startIndex: Int, endIndex: Int): MemorySpan

/**
 * Returns a read-only span over the little-endian values of [array] from [startIndex] (inclusive) to [endIndex]
 * (exclusive): a view of the array where JS can see it, and a copy otherwise.
 */
@PublishedApi
internal expect fun arraySpan(array: IntArray, startIndex: Int, endIndex: Int): MemorySpan

/**
 * Returns a read-only span over the little-endian values of [array] from [startIndex] (inclusive) to [endIndex]
 * (exclusive): a view of the array where JS can see it, and a copy otherwise.
 */
@PublishedApi
internal expect fun arraySpan(array: LongArray, startIndex: Int, endIndex: Int): MemorySpan

/**
 * Returns a read-only span over the little-endian values of [array] from [startIndex] (inclusive) to [endIndex]
 * (exclusive): a view of the array where JS can see it, and a copy otherwise.
 */
@PublishedApi
internal expect fun arraySpan(array: FloatArray, startIndex: Int, endIndex: Int): MemorySpan

/**
 * Returns a read-only span over the little-endian values of [array] from [startIndex] (inclusive) to [endIndex]
 * (exclusive): a view of the array where JS can see it, and a copy otherwise.
 */
@PublishedApi
internal expect fun arraySpan(array: DoubleArray, startIndex: Int, endIndex: Int): MemorySpan
