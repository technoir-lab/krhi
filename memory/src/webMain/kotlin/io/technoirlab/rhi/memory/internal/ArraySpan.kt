package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan

// One function for each array type is all that the inline withArraySpan functions expose to the code they are inlined into.

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
