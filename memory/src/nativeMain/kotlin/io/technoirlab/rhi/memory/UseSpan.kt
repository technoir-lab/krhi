@file:OptIn(ExperimentalContracts::class, ExperimentalForeignApi::class)

package io.technoirlab.rhi.memory

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal actual inline fun <R> withArraySpan(array: ByteArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val byteCount = checkArraySpan(startIndex, endIndex, array.size, Byte.SIZE_BYTES)
    if (byteCount == 0) return block(MemorySpan(null, 0))
    return array.usePinnedOnce { block(MemorySpan(it.addressOf(startIndex), byteCount.toLong())) }
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: ShortArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val byteCount = checkArraySpan(startIndex, endIndex, array.size, Short.SIZE_BYTES)
    if (byteCount == 0) return block(MemorySpan(null, 0))
    return array.usePinnedOnce { block(MemorySpan(it.addressOf(startIndex), byteCount.toLong())) }
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: IntArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val byteCount = checkArraySpan(startIndex, endIndex, array.size, Int.SIZE_BYTES)
    if (byteCount == 0) return block(MemorySpan(null, 0))
    return array.usePinnedOnce { block(MemorySpan(it.addressOf(startIndex), byteCount.toLong())) }
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: LongArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val byteCount = checkArraySpan(startIndex, endIndex, array.size, Long.SIZE_BYTES)
    if (byteCount == 0) return block(MemorySpan(null, 0))
    return array.usePinnedOnce { block(MemorySpan(it.addressOf(startIndex), byteCount.toLong())) }
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: FloatArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val byteCount = checkArraySpan(startIndex, endIndex, array.size, Float.SIZE_BYTES)
    if (byteCount == 0) return block(MemorySpan(null, 0))
    return array.usePinnedOnce { block(MemorySpan(it.addressOf(startIndex), byteCount.toLong())) }
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: DoubleArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val byteCount = checkArraySpan(startIndex, endIndex, array.size, Double.SIZE_BYTES)
    if (byteCount == 0) return block(MemorySpan(null, 0))
    return array.usePinnedOnce { block(MemorySpan(it.addressOf(startIndex), byteCount.toLong())) }
}

/**
 * Calls [block] once with this object pinned. It is `usePinned` with the contract that the function of kotlinx.cinterop does
 * not declare, so that the compiler can check the contract of the functions above.
 */
@PublishedApi
internal inline fun <T : Any, R> T.usePinnedOnce(block: (Pinned<T>) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val pinned = pin()
    try {
        return block(pinned)
    } finally {
        pinned.unpin()
    }
}
