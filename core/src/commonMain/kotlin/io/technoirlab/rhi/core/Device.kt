package io.technoirlab.rhi.core

import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.rhi.core.geometry.VertexLayout
import kotlinx.io.Source

interface Device : AutoCloseable {
    fun createDepthStencilBuffer(extent: Extent2D, format: Format): Texture

    fun createVertexBuffer(source: Source, vertexCount: UInt, vertexLayout: VertexLayout): VertexBuffer

    fun createIndexBuffer(source: Source, indexCount: UInt, indexType: IndexType): IndexBuffer

    fun createShader(type: ShaderType, source: Source, entryPoint: String = "main"): Shader

    @Suppress("LongParameterList")
    fun createGraphicsState(
        renderTarget: RenderTarget,
        vertexBuffer: VertexBuffer,
        indexBuffer: IndexBuffer,
        primitiveType: PrimitiveType,
        vertexShader: Shader,
        fragmentShader: Shader,
        rasterState: RasterState = RasterState(),
        blendState: BlendState = BlendState(),
        depthStencilState: DepthStencilState = DepthStencilState(),
        pushConstants: ByteArray? = null,
    ): GraphicsState
}
