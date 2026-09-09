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
import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.rhi.core.geometry.VertexLayout
import kotlinx.io.Source
import web.gpu.GPU
import web.gpu.GPUCanvasConfiguration
import web.gpu.GPUCanvasContext
import web.gpu.GPUDevice
import web.gpu.GPUTextureFormat
import web.gpu.ID
import web.gpu.requestAdapter
import web.gpu.requestDevice
import kotlin.js.ExperimentalWasmJsInterop

class WebGpuDevice private constructor(
    private val device: GPUDevice,
    private val context: GPUCanvasContext,
) : Device {
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

    override fun close() {
        try {
            context.unconfigure()
        } finally {
            device.destroy()
        }
    }

    companion object {
        /**
         * Requests a browser GPU device and configures the window's canvas for WebGPU presentation.
         */
        suspend fun create(window: WindowHandle): WebGpuDevice {
            val gpu = checkNotNull(getBrowserGpu()) { "WebGPU is unavailable. Use a supported browser over HTTPS or localhost." }
            val adapter = checkNotNull(gpu.requestAdapter()) { "No WebGPU adapter is available." }
            val context = checkNotNull(window.canvas.getContext(GPUCanvasContext.ID)) { "Could not create a WebGPU canvas context." }
            val device = adapter.requestDevice()
            try {
                context.configure(createCanvasConfiguration(device, gpu.getPreferredCanvasFormat()))
                return WebGpuDevice(device, context)
            } catch (e: Throwable) {
                device.destroy()
                throw e
            }
        }
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private fun getBrowserGpu(): GPU = js("navigator.gpu || null")

@OptIn(ExperimentalWasmJsInterop::class)
private fun createCanvasConfiguration(device: GPUDevice, format: GPUTextureFormat): GPUCanvasConfiguration =
    js("({ device: device, format: format })")
