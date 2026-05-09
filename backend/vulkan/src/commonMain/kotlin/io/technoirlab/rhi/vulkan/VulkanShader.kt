package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.Shader
import io.technoirlab.rhi.core.ShaderType
import io.technoirlab.vulkan.ShaderModule

internal class VulkanShader(
    override val type: ShaderType,
    override val entryPoint: String,
    val shader: ShaderModule,
) : Shader {

    override fun close() {
        shader.close()
    }
}
