package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.BlendState
import io.technoirlab.rhi.core.DepthStencilState
import io.technoirlab.rhi.core.GraphicsState
import io.technoirlab.rhi.core.RasterState
import io.technoirlab.rhi.core.Shader
import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.vulkan.Pipeline
import io.technoirlab.vulkan.PipelineLayout

@Suppress("LongParameterList")
internal class VulkanGraphicsState(
    val pipeline: Pipeline,
    val pipelineLayout: PipelineLayout,
    override val vertexBuffer: VertexBuffer,
    override val indexBuffer: IndexBuffer,
    override val primitiveType: PrimitiveType,
    override val vertexShader: Shader,
    override val fragmentShader: Shader,
    override val rasterState: RasterState,
    override val blendState: BlendState,
    override val depthStencilState: DepthStencilState,
    override var pushConstants: ByteArray? = null,
) : GraphicsState {

    override fun close() {
        pipeline.close()
        pipelineLayout.close()
    }
}
