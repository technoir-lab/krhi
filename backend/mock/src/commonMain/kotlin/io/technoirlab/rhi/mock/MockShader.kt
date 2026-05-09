package io.technoirlab.rhi.mock

import io.technoirlab.rhi.core.Shader
import io.technoirlab.rhi.core.ShaderType

internal class MockShader(
    override val type: ShaderType,
    override val entryPoint: String,
) : Shader {
    override fun close() = Unit
}
