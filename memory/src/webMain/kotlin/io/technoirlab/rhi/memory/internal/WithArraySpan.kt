@file:OptIn(ExperimentalContracts::class)

package io.technoirlab.rhi.memory.internal

import io.technoirlab.rhi.memory.MemorySpan
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal actual inline fun <R> withArraySpan(array: ByteArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: ShortArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: IntArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: LongArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: FloatArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}

@PublishedApi
internal actual inline fun <R> withArraySpan(array: DoubleArray, startIndex: Int, endIndex: Int, block: (MemorySpan) -> R): R {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block(arraySpan(array, startIndex, endIndex))
}
