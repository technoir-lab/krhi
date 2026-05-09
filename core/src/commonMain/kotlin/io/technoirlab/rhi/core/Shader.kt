package io.technoirlab.rhi.core

interface Shader : AutoCloseable {
    val type: ShaderType
    val entryPoint: String
}

enum class ShaderType {
    Compute,
    Vertex,
    Hull,
    Domain,
    Geometry,
    Fragment,
}
