package io.technoirlab.rhi.memory

import kotlin.jvm.JvmInline

/**
 * A range handed out by a [TlsfAllocator], or [None].
 *
 * It packs the index of the allocator's node for the range and the node's generation into one [Int], so allocating and
 * freeing create no objects. The generation changes each time the node is freed, which is how the allocator refuses a
 * handle that has been freed, even after its node has been reused. A stale handle goes unnoticed only when its node has
 * since been freed a multiple of 1024 times.
 */
@JvmInline
value class OffsetAllocation internal constructor(internal val bits: Int) {
    internal val node: Int
        get() = bits and NODE_MASK

    internal val generation: Int
        get() = bits ushr GENERATION_SHIFT

    /**
     * Whether this is [None].
     */
    val isNone: Boolean
        get() = bits == NONE_BITS

    override fun toString(): String = if (isNone) "OffsetAllocation.None" else "OffsetAllocation(node=$node, generation=$generation)"

    companion object {
        private const val NONE_BITS = -1

        internal const val GENERATION_SHIFT = 22
        internal const val NODE_MASK = (1 shl GENERATION_SHIFT) - 1
        internal const val GENERATION_MASK = (1 shl (Int.SIZE_BITS - GENERATION_SHIFT)) - 1

        /**
         * The largest node count. The last index stays unused, so that no node and generation pack into [None]'s bits.
         */
        internal const val MAX_NODES = NODE_MASK

        /**
         * The result of an allocation that did not fit. Freeing it does nothing.
         */
        val None = OffsetAllocation(NONE_BITS)

        internal fun of(node: Int, generation: Int) = OffsetAllocation((generation shl GENERATION_SHIFT) or node)
    }
}
