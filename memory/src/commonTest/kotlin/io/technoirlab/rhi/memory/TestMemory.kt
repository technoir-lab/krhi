package io.technoirlab.rhi.memory

/**
 * Calls [block] with a writable span over [size] bytes of memory that lives until [block] returns.
 */
expect fun withTestMemory(size: Int, block: (MutableMemorySpan) -> Unit)

/**
 * Returns a copy of the bytes of the span.
 */
fun MemorySpan.toByteArray(): ByteArray = ByteArray(size).also { read(it) }

/**
 * Returns [count] bytes counting up from [first].
 */
fun byteSequence(count: Int, first: Int = 1): ByteArray = ByteArray(count) { (first + it).toByte() }
