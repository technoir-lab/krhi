package io.technoirlab.rhi.memory

import js.buffer.ArrayBuffer

actual fun withTestMemory(size: Int, block: (MutableMemorySpan) -> Unit) = block(MutableMemorySpan(ArrayBuffer(size)))
