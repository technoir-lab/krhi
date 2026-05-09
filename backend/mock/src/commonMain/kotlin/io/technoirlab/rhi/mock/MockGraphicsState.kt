package io.technoirlab.rhi.mock

import io.technoirlab.rhi.core.BlendState
import io.technoirlab.rhi.core.DepthStencilState
import io.technoirlab.rhi.core.GraphicsState
import io.technoirlab.rhi.core.RasterState
import io.technoirlab.rhi.core.Shader
import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexBuffer

@Suppress("LongParameterList")
internal class MockGraphicsState(
    override val vertexBuffer: VertexBuffer,
    override val indexBuffer: IndexBuffer,
    override val primitiveType: PrimitiveType,
    override val vertexShader: Shader,
    override val fragmentShader: Shader,
    override val rasterState: RasterState,
    override val blendState: BlendState,
    override val depthStencilState: DepthStencilState,
    override var pushConstants: ByteArray?,
) : GraphicsState {
    override fun close() = Unit
}
