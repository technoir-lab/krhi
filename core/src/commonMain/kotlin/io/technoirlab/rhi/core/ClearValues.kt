package io.technoirlab.rhi.core

import dev.romainguy.kotlin.math.Float4

data class ClearValues(
    val color: List<Float4>,
    val depth: Float = 1.0f,
    val stencil: UInt = 0u,
)
