package io.technoirlab.rhi.vulkan

import io.technoirlab.vulkan.memory.DeviceMemory
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.NativePlacement
import kotlinx.cinterop.UIntVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.plus
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.io.EOFException
import kotlinx.io.Source
import platform.posix.memcpy

private const val READ_BUFFER_SIZE = 8192

internal fun NativePlacement.allocArrayOf(elements: List<UInt>): CArrayPointer<UIntVar> {
    val array = allocArray<UIntVar>(elements.size)
    var index = 0
    while (index < elements.size) {
        array[index] = elements[index]
        ++index
    }
    return array
}

/**
 * Copy exactly [size] bytes from [source] into host-visible device memory starting at [offset].
 */
internal fun DeviceMemory.copyFrom(source: Source, size: ULong, offset: ULong = 0uL) {
    val mappedPtr = map(size, offset).reinterpret<ByteVar>()
    try {
        ByteArray(READ_BUFFER_SIZE).usePinned { pinned ->
            var totalRead = 0uL
            while (totalRead < size) {
                val toRead = minOf(READ_BUFFER_SIZE.toULong(), size - totalRead)
                val read = source.readAtMostTo(pinned.get(), 0, toRead.toInt())
                if (read <= 0) {
                    throw EOFException("Not enough data in source: expected $size bytes, but read $totalRead bytes")
                }
                memcpy(mappedPtr + totalRead.toLong(), pinned.addressOf(0), read.convert())
                totalRead += read.toULong()
            }
        }
    } finally {
        unmap()
    }
}
