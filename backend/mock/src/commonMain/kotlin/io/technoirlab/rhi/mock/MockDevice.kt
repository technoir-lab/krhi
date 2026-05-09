package io.technoirlab.rhi.mock

import io.technoirlab.rhi.core.BlendState
import io.technoirlab.rhi.core.DepthStencilState
import io.technoirlab.rhi.core.Device
import io.technoirlab.rhi.core.Extent2D
import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.GraphicsState
import io.technoirlab.rhi.core.RasterState
import io.technoirlab.rhi.core.RenderTarget
import io.technoirlab.rhi.core.Shader
import io.technoirlab.rhi.core.ShaderType
import io.technoirlab.rhi.core.Texture
import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.rhi.core.geometry.VertexLayout
import io.technoirlab.rhi.core.geometry.sizeInBytes
import io.technoirlab.rhi.mock.geometry.MockIndexBuffer
import io.technoirlab.rhi.mock.geometry.MockVertexBuffer
import kotlinx.io.Source

class MockDevice : Device {
    override fun createDepthStencilBuffer(extent: Extent2D, format: Format): Texture = MockTexture(extent, format, sampleCount = 1u)

    override fun createVertexBuffer(source: Source, vertexCount: UInt, vertexLayout: VertexLayout): VertexBuffer =
        MockVertexBuffer(vertexCount.toULong() * vertexLayout.vertexSize, vertexCount, vertexLayout)

    override fun createIndexBuffer(source: Source, indexCount: UInt, indexType: IndexType): IndexBuffer =
        MockIndexBuffer(indexCount.toULong() * indexType.sizeInBytes, indexCount, indexType)

    override fun createShader(type: ShaderType, source: Source, entryPoint: String): Shader = MockShader(type, entryPoint)

    override fun createGraphicsState(
        renderTarget: RenderTarget,
        vertexBuffer: VertexBuffer,
        indexBuffer: IndexBuffer,
        primitiveType: PrimitiveType,
        vertexShader: Shader,
        fragmentShader: Shader,
        rasterState: RasterState,
        blendState: BlendState,
        depthStencilState: DepthStencilState,
        pushConstants: ByteArray?,
    ): GraphicsState =
        MockGraphicsState(
            vertexBuffer = vertexBuffer,
            indexBuffer = indexBuffer,
            primitiveType = primitiveType,
            vertexShader = vertexShader,
            fragmentShader = fragmentShader,
            rasterState = rasterState,
            blendState = blendState,
            depthStencilState = depthStencilState,
            pushConstants = pushConstants,
        )

    override fun close() = Unit
}
