package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan
import io.technoirlab.rhi.memory.MutableMemorySpan

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

/**
 * Copies an array range that the caller has checked and that is not empty into the span.
 */
internal expect fun MutableMemorySpan.writeArray(offset: Int, source: ByteArray, startIndex: Int, endIndex: Int)

/**
 * Copies an array range that the caller has checked and that is not empty into the span as little-endian values.
 */
internal expect fun MutableMemorySpan.writeArray(offset: Int, source: ShortArray, startIndex: Int, endIndex: Int)

/**
 * Copies an array range that the caller has checked and that is not empty into the span as little-endian values.
 */
internal expect fun MutableMemorySpan.writeArray(offset: Int, source: IntArray, startIndex: Int, endIndex: Int)

/**
 * Copies an array range that the caller has checked and that is not empty into the span as little-endian values.
 */
internal expect fun MutableMemorySpan.writeArray(offset: Int, source: LongArray, startIndex: Int, endIndex: Int)

/**
 * Copies an array range that the caller has checked and that is not empty into the span as little-endian values.
 */
internal expect fun MutableMemorySpan.writeArray(offset: Int, source: FloatArray, startIndex: Int, endIndex: Int)

/**
 * Copies an array range that the caller has checked and that is not empty into the span as little-endian values.
 */
internal expect fun MutableMemorySpan.writeArray(offset: Int, source: DoubleArray, startIndex: Int, endIndex: Int)
