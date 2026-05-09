package io.technoirlab.rhi.core.geometry

data class VertexAttribute(
    val type: Type,
    val offset: UInt,
) {
    enum class Type {
        Float,
        Float2,
        Float3,
        Float4,
    }
}

val VertexAttribute.Type.sizeInBytes: UInt
    get() = when (this) {
        VertexAttribute.Type.Float -> 4u
        VertexAttribute.Type.Float2 -> 8u
        VertexAttribute.Type.Float3 -> 12u
        VertexAttribute.Type.Float4 -> 16u
    }
