package io.technoirlab.rhi.webgpu

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
import kotlinx.io.Source

class WebGpuDevice : Device {
    override fun createDepthStencilBuffer(extent: Extent2D, format: Format): Texture {
        TODO("Not yet implemented")
    }

    override fun createVertexBuffer(source: Source, vertexCount: UInt, vertexLayout: VertexLayout): VertexBuffer {
        TODO("Not yet implemented")
    }

    override fun createIndexBuffer(source: Source, indexCount: UInt, indexType: IndexType): IndexBuffer {
        TODO("Not yet implemented")
    }

    override fun createShader(type: ShaderType, source: Source, entryPoint: String): Shader {
        TODO("Not yet implemented")
    }

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
    ): GraphicsState {
        TODO("Not yet implemented")
    }

    override fun close() = Unit
}
