@file:OptIn(ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap

actual fun withTestMemory(size: Int, block: (MutableMemorySpan) -> Unit) {
    val memory = nativeHeap.allocArray<ByteVar>(maxOf(size, 1))
    try {
        block(MutableMemorySpan(memory, size.toLong()))
    } finally {
        nativeHeap.free(memory)
    }
}
