package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan

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
