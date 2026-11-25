@file:OptIn(ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.interpretPointed
import kotlinx.cinterop.value

// Little-endian access at any address. Kotlin/Native's own accessors assume natural alignment, so values are assembled from
// bytes. Every interpretPointed call checks its address for null; taking all of them before the first byte is read or written
// keeps the byte accesses together, where the code generator can merge them into one load or store that assumes no alignment.
// It does in an accessor compiled on its own. Once LLVM inlines the accessor into a loop, it may keep the byte accesses apart.
// The functions are inline so that an accessor holds its access directly and does not call another function for it.

internal inline fun loadByte(address: NativePtr): Byte = interpretPointed<ByteVar>(address).value

internal inline fun loadShort(address: NativePtr): Short {
    val b0 = interpretPointed<ByteVar>(address)
    val b1 = interpretPointed<ByteVar>(address + 1L)
    return ((b0.value.toInt() and 0xFF) or ((b1.value.toInt() and 0xFF) shl 8)).toShort()
}

internal inline fun loadInt(address: NativePtr): Int {
    val b0 = interpretPointed<ByteVar>(address)
    val b1 = interpretPointed<ByteVar>(address + 1L)
    val b2 = interpretPointed<ByteVar>(address + 2L)
    val b3 = interpretPointed<ByteVar>(address + 3L)
    return (b0.value.toInt() and 0xFF) or
        ((b1.value.toInt() and 0xFF) shl 8) or
        ((b2.value.toInt() and 0xFF) shl 16) or
        ((b3.value.toInt() and 0xFF) shl 24)
}

internal inline fun loadLong(address: NativePtr): Long {
    val b0 = interpretPointed<ByteVar>(address)
    val b1 = interpretPointed<ByteVar>(address + 1L)
    val b2 = interpretPointed<ByteVar>(address + 2L)
    val b3 = interpretPointed<ByteVar>(address + 3L)
    val b4 = interpretPointed<ByteVar>(address + 4L)
    val b5 = interpretPointed<ByteVar>(address + 5L)
    val b6 = interpretPointed<ByteVar>(address + 6L)
    val b7 = interpretPointed<ByteVar>(address + 7L)
    return (b0.value.toLong() and 0xFF) or
        ((b1.value.toLong() and 0xFF) shl 8) or
        ((b2.value.toLong() and 0xFF) shl 16) or
        ((b3.value.toLong() and 0xFF) shl 24) or
        ((b4.value.toLong() and 0xFF) shl 32) or
        ((b5.value.toLong() and 0xFF) shl 40) or
        ((b6.value.toLong() and 0xFF) shl 48) or
        ((b7.value.toLong() and 0xFF) shl 56)
}

internal inline fun storeByte(address: NativePtr, value: Byte) {
    interpretPointed<ByteVar>(address).value = value
}

internal inline fun storeShort(address: NativePtr, value: Short) {
    val b0 = interpretPointed<ByteVar>(address)
    val b1 = interpretPointed<ByteVar>(address + 1L)
    val bits = value.toInt()
    b0.value = bits.toByte()
    b1.value = (bits ushr 8).toByte()
}

internal inline fun storeInt(address: NativePtr, value: Int) {
    val b0 = interpretPointed<ByteVar>(address)
    val b1 = interpretPointed<ByteVar>(address + 1L)
    val b2 = interpretPointed<ByteVar>(address + 2L)
    val b3 = interpretPointed<ByteVar>(address + 3L)
    b0.value = value.toByte()
    b1.value = (value ushr 8).toByte()
    b2.value = (value ushr 16).toByte()
    b3.value = (value ushr 24).toByte()
}

internal inline fun storeLong(address: NativePtr, value: Long) {
    val b0 = interpretPointed<ByteVar>(address)
    val b1 = interpretPointed<ByteVar>(address + 1L)
    val b2 = interpretPointed<ByteVar>(address + 2L)
    val b3 = interpretPointed<ByteVar>(address + 3L)
    val b4 = interpretPointed<ByteVar>(address + 4L)
    val b5 = interpretPointed<ByteVar>(address + 5L)
    val b6 = interpretPointed<ByteVar>(address + 6L)
    val b7 = interpretPointed<ByteVar>(address + 7L)
    b0.value = value.toByte()
    b1.value = (value ushr 8).toByte()
    b2.value = (value ushr 16).toByte()
    b3.value = (value ushr 24).toByte()
    b4.value = (value ushr 32).toByte()
    b5.value = (value ushr 40).toByte()
    b6.value = (value ushr 48).toByte()
    b7.value = (value ushr 56).toByte()
}
