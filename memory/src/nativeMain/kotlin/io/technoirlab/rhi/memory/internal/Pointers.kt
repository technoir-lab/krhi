@file:OptIn(ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory.internal

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import platform.posix.memmove

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
