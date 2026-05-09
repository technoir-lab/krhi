package io.technoirlab.rhi.core

data class Viewport(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val minDepth: Float = 0.0f,
    val maxDepth: Float = 1.0f,
)
