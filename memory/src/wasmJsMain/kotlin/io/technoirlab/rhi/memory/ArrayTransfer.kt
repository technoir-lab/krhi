@file:OptIn(ExperimentalWasmJsInterop::class, UnsafeWasmMemoryApi::class)

package io.technoirlab.rhi.memory

import js.buffer.ArrayBuffer
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.math.min
import kotlin.wasm.unsafe.Pointer
import kotlin.wasm.unsafe.UnsafeWasmMemoryApi
import kotlin.wasm.unsafe.WebAssembly
import kotlin.wasm.unsafe.wasmMemory
import kotlin.wasm.unsafe.withScopedMemoryAllocator

// A Kotlin array on Wasm lives in the Wasm heap, which JS cannot see. Arrays therefore move through a small block of linear
// memory: Kotlin fills it without leaving Wasm, and each block moves in one copy. The block is reused from chunk
// to chunk, so linear memory, which never shrinks, does not grow with the array.

/**
 * The size of the block of linear memory that array transfer moves at a time.
 */
private const val CHUNK_SIZE = 16 * 1024

internal actual fun MemorySpan.readArray(offset: Int, destination: ByteArray, startIndex: Int, endIndex: Int) =
    readChunks(offset, startIndex, endIndex, Byte.SIZE_BYTES) { pointer, index -> destination[index] = pointer.loadByte() }

internal actual fun MemorySpan.readArray(offset: Int, destination: ShortArray, startIndex: Int, endIndex: Int) =
    readChunks(offset, startIndex, endIndex, Short.SIZE_BYTES) { pointer, index -> destination[index] = pointer.loadShort() }

internal actual fun MemorySpan.readArray(offset: Int, destination: IntArray, startIndex: Int, endIndex: Int) =
    readChunks(offset, startIndex, endIndex, Int.SIZE_BYTES) { pointer, index -> destination[index] = pointer.loadInt() }

internal actual fun MemorySpan.readArray(offset: Int, destination: LongArray, startIndex: Int, endIndex: Int) =
    readChunks(offset, startIndex, endIndex, Long.SIZE_BYTES) { pointer, index -> destination[index] = pointer.loadLong() }

internal actual fun MemorySpan.readArray(offset: Int, destination: FloatArray, startIndex: Int, endIndex: Int) =
    readChunks(offset, startIndex, endIndex, Float.SIZE_BYTES) { pointer, index ->
        destination[index] = Float.fromBits(pointer.loadInt())
    }

internal actual fun MemorySpan.readArray(offset: Int, destination: DoubleArray, startIndex: Int, endIndex: Int) =
    readChunks(offset, startIndex, endIndex, Double.SIZE_BYTES) { pointer, index ->
        destination[index] = Double.fromBits(pointer.loadLong())
    }

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: ByteArray, startIndex: Int, endIndex: Int) =
    writeChunks(offset, startIndex, endIndex, Byte.SIZE_BYTES) { pointer, index -> pointer.storeByte(source[index]) }

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: ShortArray, startIndex: Int, endIndex: Int) =
    writeChunks(offset, startIndex, endIndex, Short.SIZE_BYTES) { pointer, index -> pointer.storeShort(source[index]) }

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: IntArray, startIndex: Int, endIndex: Int) =
    writeChunks(offset, startIndex, endIndex, Int.SIZE_BYTES) { pointer, index -> pointer.storeInt(source[index]) }

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: LongArray, startIndex: Int, endIndex: Int) =
    writeChunks(offset, startIndex, endIndex, Long.SIZE_BYTES) { pointer, index -> pointer.storeLong(source[index]) }

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: FloatArray, startIndex: Int, endIndex: Int) =
    writeChunks(offset, startIndex, endIndex, Float.SIZE_BYTES) { pointer, index -> pointer.storeInt(source[index].toRawBits()) }

internal actual fun MutableMemorySpan.writeArray(offset: Int, source: DoubleArray, startIndex: Int, endIndex: Int) =
    writeChunks(offset, startIndex, endIndex, Double.SIZE_BYTES) { pointer, index -> pointer.storeLong(source[index].toRawBits()) }

/**
 * Moves the elements from [startIndex] to [endIndex] out of the span a chunk at a time, calling [load] for each element with
 * its address in linear memory and its array index.
 */
private inline fun MemorySpan.readChunks(offset: Int, startIndex: Int, endIndex: Int, elementSize: Int, load: (Pointer, Int) -> Unit) {
    withChunks(startIndex, endIndex, elementSize) { linearMemory, chunk, firstIndex, count ->
        val sourceOffset = byteOffset + offset + (firstIndex - startIndex) * elementSize
        copyBytes(linearMemory, chunk.address.toInt(), buffer, sourceOffset, count * elementSize)
        for (index in 0 until count) {
            load(chunk + index * elementSize, firstIndex + index)
        }
    }
}

/**
 * Moves the elements from [startIndex] to [endIndex] into the span a chunk at a time, calling [store] for each element with
 * its address in linear memory and its array index.
 */
private inline fun MutableMemorySpan.writeChunks(
    offset: Int,
    startIndex: Int,
    endIndex: Int,
    elementSize: Int,
    store: (Pointer, Int) -> Unit,
) {
    withChunks(startIndex, endIndex, elementSize) { linearMemory, chunk, firstIndex, count ->
        for (index in 0 until count) {
            store(chunk + index * elementSize, firstIndex + index)
        }
        val destinationOffset = byteOffset + offset + (firstIndex - startIndex) * elementSize
        copyBytes(buffer, destinationOffset, linearMemory, chunk.address.toInt(), count * elementSize)
    }
}

/**
 * Allocates one block of linear memory and calls [action] for each chunk of the range, with the buffer of linear memory, the
 * block's address in it, the chunk's first array index and its element count.
 */
private inline fun withChunks(
    startIndex: Int,
    endIndex: Int,
    elementSize: Int,
    action: (linearMemory: ArrayBuffer, chunk: Pointer, firstIndex: Int, count: Int) -> Unit,
) {
    val elementsPerChunk = min(endIndex - startIndex, CHUNK_SIZE / elementSize)
    withScopedMemoryAllocator { allocator ->
        val chunk = allocator.allocate(elementsPerChunk * elementSize)
        // Taken after the allocation, which may grow linear memory and detach any earlier view of it.
        val linearMemory = memoryBuffer(wasmMemory)
        var firstIndex = startIndex
        while (firstIndex < endIndex) {
            val count = min(elementsPerChunk, endIndex - firstIndex)
            action(linearMemory, chunk, firstIndex, count)
            firstIndex += count
        }
    }
}

private fun memoryBuffer(memory: WebAssembly.Memory): ArrayBuffer = js("memory.buffer")
