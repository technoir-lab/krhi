package io.technoirlab.rhi.core

data class Extent2D(
    val width: UInt,
    val height: UInt,
) {
    override fun toString(): String = "${width}x$height"
}
