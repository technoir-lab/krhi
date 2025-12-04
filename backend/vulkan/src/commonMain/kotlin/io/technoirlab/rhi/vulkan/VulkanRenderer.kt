package io.technoirlab.rhi.vulkan

import io.github.oshai.kotlinlogging.KotlinLogging
import io.technoirlab.rhi.core.Device
import io.technoirlab.rhi.core.FrameState
import io.technoirlab.rhi.core.GraphicsState
import io.technoirlab.rhi.core.RenderTarget
import io.technoirlab.rhi.core.Renderer
import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.rhi.core.config.RendererConfig
import io.technoirlab.rhi.vulkan.geometry.VulkanIndexBuffer
import io.technoirlab.rhi.vulkan.geometry.VulkanVertexBuffer
import io.technoirlab.volk.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT
import io.technoirlab.volk.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT
import io.technoirlab.volk.VK_ACCESS_2_NONE
import io.technoirlab.volk.VK_API_VERSION_1_4
import io.technoirlab.volk.VK_ATTACHMENT_LOAD_OP_CLEAR
import io.technoirlab.volk.VK_ATTACHMENT_STORE_OP_STORE
import io.technoirlab.volk.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT
import io.technoirlab.volk.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
import io.technoirlab.volk.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
import io.technoirlab.volk.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR
import io.technoirlab.volk.VK_PIPELINE_BIND_POINT_GRAPHICS
import io.technoirlab.volk.VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT
import io.technoirlab.volk.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
import io.technoirlab.volk.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT
import io.technoirlab.volk.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT
import io.technoirlab.volk.VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT
import io.technoirlab.volk.VK_QUEUE_FAMILY_IGNORED
import io.technoirlab.volk.VK_SHADER_STAGE_VERTEX_BIT
import io.technoirlab.volk.VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO
import io.technoirlab.volk.VkAccessFlags2
import io.technoirlab.volk.VkImageLayout
import io.technoirlab.volk.VkPipelineStageFlags2
import io.technoirlab.volk.VkRenderingAttachmentInfo
import io.technoirlab.vulkan.CommandBuffer
import io.technoirlab.vulkan.Surface
import io.technoirlab.vulkan.Vulkan
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString

class VulkanRenderer : Renderer {
    private val logger = KotlinLogging.logger("VulkanRenderer")
    private val minApiVersion = MIN_API_VERSION
    private val vulkan: Vulkan
    private val instance: VulkanInstance
    private var device: VulkanDevice? = null
    private var surface: Surface? = null
    private var swapChain: VulkanSwapChain? = null

    init {
        logger.info { "Initializing Vulkan renderer" }
        vulkan = Vulkan()

        val apiVersion = vulkan.instanceVersion
        check(apiVersion >= minApiVersion) {
            "Vulkan API version ${versionToString(apiVersion)} is lower than the minimum required ${versionToString(minApiVersion)}"
        }

        instance = memScoped {
            val supportedLayers = vulkan.getSupportedLayers()
            if (supportedLayers.isNotEmpty()) {
                logger.info { "Supported instance layers: [${supportedLayers.joinToString()}]" }
            }
            val supportedExtensions = vulkan.getSupportedExtensions()
            if (supportedExtensions.isNotEmpty()) {
                logger.info { "Supported instance extensions: [${supportedExtensions.joinToString()}]" }
            }
            val instanceLayers = supportedLayers.filter(
                requiredLayers = REQUIRED_LAYERS,
                optionalLayers = OPTIONAL_LAYERS
            )
            val instanceExtensions = supportedExtensions.filter(
                requiredExtensions = REQUIRED_INSTANCE_EXTENSIONS,
                optionalExtensions = OPTIONAL_INSTANCE_EXTENSIONS
            )
            VulkanInstance(vulkan, apiVersion, instanceLayers, instanceExtensions)
        }
    }

    override fun createDevice(window: WindowHandle, config: RendererConfig): Device = memScoped {
        logger.info { "Creating surface" }
        val surface = instance.createSurface(window).also { surface = it }

        val (physicalDevice, deviceSpec) = selectPhysicalDevice(instance, surface, minApiVersion, config.deviceName)

        val supportedDeviceExtensions = physicalDevice.getSupportedExtensions()
        if (supportedDeviceExtensions.isNotEmpty()) {
            logger.info { "Supported device extensions: [${supportedDeviceExtensions.joinToString()}]" }
        }
        val deviceExtensions = supportedDeviceExtensions.filter(
            requiredExtensions = REQUIRED_DEVICE_EXTENSIONS,
            optionalExtensions = OPTIONAL_DEVICE_EXTENSIONS
        )
        val device = physicalDevice.createDevice(deviceSpec, deviceExtensions).also { device = it }

        swapChain = device.createSwapChain(surface, window, config)
        return device
    }

    override fun prepare(): FrameState = memScoped {
        val swapChain = checkNotNull(swapChain)

        val frameState = swapChain.acquireNextTexture()
        val commandBuffer = frameState.commandBuffer
        commandBuffer.begin(usageFlags = VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
        commandBuffer.transitionImageLayout(
            texture = frameState.texture,
            oldLayout = frameState.texture.layout,
            newLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
            srcAccessMask = VK_ACCESS_2_NONE,
            dstAccessMask = VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
            srcStage = VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT,
            dstStage = VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
        )
        frameState.depthStencil?.let { depthStencil ->
            commandBuffer.transitionImageLayout(
                texture = depthStencil,
                oldLayout = depthStencil.layout,
                newLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                srcAccessMask = VK_ACCESS_2_NONE,
                dstAccessMask = VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                srcStage = VK_PIPELINE_STAGE_2_TOP_OF_PIPE_BIT,
                dstStage = VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT
            )
        }
        commandBuffer.beginRendering(swapChain)
        return frameState
    }

    override fun present(frameState: FrameState) = memScoped {
        require(frameState is VulkanFrameState)
        val swapChain = checkNotNull(swapChain)

        val commandBuffer = frameState.commandBuffer
        commandBuffer.endRendering()
        commandBuffer.transitionImageLayout(
            texture = frameState.texture,
            oldLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
            newLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
            srcAccessMask = VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT,
            dstAccessMask = VK_ACCESS_2_NONE,
            srcStage = VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT,
            dstStage = VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT
        )
        frameState.depthStencil?.let { depthStencil ->
            commandBuffer.transitionImageLayout(
                texture = depthStencil,
                oldLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                newLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                srcAccessMask = VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT,
                dstAccessMask = VK_ACCESS_2_NONE,
                srcStage = VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT,
                dstStage = VK_PIPELINE_STAGE_2_BOTTOM_OF_PIPE_BIT
            )
        }
        commandBuffer.end()

        swapChain.submit(frameState)
        swapChain.present(frameState)
    }

    override fun render(frameState: FrameState, graphicsState: GraphicsState) = memScoped {
        require(frameState is VulkanFrameState)
        require(graphicsState is VulkanGraphicsState)
        require(graphicsState.vertexBuffer is VulkanVertexBuffer)
        require(graphicsState.indexBuffer is VulkanIndexBuffer)

        val commandBuffer = frameState.commandBuffer

        commandBuffer.bindPipeline(VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsState.pipeline)
        commandBuffer.setPrimitiveTopology(graphicsState.primitiveType.toVkPrimitiveTopology())
        commandBuffer.setViewportWithCount(count = 1u) {
            x = 0.0f
            y = 0.0f
            minDepth = 0.0f
            maxDepth = 1.0f
            width = frameState.texture.extent.width.toFloat()
            height = frameState.texture.extent.height.toFloat()
        }
        commandBuffer.setScissorWithCount(count = 1u) {
            offset.x = 0
            offset.y = 0
            extent.width = frameState.texture.extent.width
            extent.height = frameState.texture.extent.height
        }
        commandBuffer.setCullMode(graphicsState.rasterState.cullMode.toVkCullMode())
        commandBuffer.setFrontFace(graphicsState.rasterState.frontFace.toVkFrontFace())

        graphicsState.pushConstants?.let { pushConstants ->
            commandBuffer.pushConstants(graphicsState.pipelineLayout, VK_SHADER_STAGE_VERTEX_BIT, pushConstants)
        }

        commandBuffer.bindVertexBuffer(graphicsState.vertexBuffer.buffer)
        commandBuffer.bindIndexBuffer(graphicsState.indexBuffer.buffer, graphicsState.indexBuffer.indexType.toVkIndexType())
        commandBuffer.drawIndexed(graphicsState.indexBuffer.indexCount)
    }

    override fun reset(): Unit = memScoped {
        swapChain?.reset()
    }

    override fun close() {
        device?.graphicsQueue?.waitIdle()
        device?.presentQueue?.waitIdle()
        swapChain?.close()
        swapChain = null
        surface?.close()
        surface = null
        device?.close()
        device = null
        instance.close()
        vulkan.close()
    }

    context(memScope: MemScope)
    private fun selectPhysicalDevice(
        instance: VulkanInstance,
        surface: Surface,
        minApiVersion: UInt,
        deviceName: String?
    ): Pair<VulkanPhysicalDevice, VulkanDeviceSpec> {
        val availableDevices = instance.getPhysicalDevices()
        check(availableDevices.isNotEmpty()) { "No devices found" }

        val deviceCompatibility = availableDevices.associateWith { it.checkCompatibility(surface, minApiVersion) }
        val (compatibleDevices, incompatibleDevices) = deviceCompatibility.entries.partition {
            it.value is VulkanDeviceCompatibility.Compatible
        }
        if (incompatibleDevices.isNotEmpty()) {
            logger.info { "Incompatible devices: [${incompatibleDevices.joinToString { "${it.key.name} (${it.value.description})" }}]" }
        }
        check(compatibleDevices.isNotEmpty()) { "No compatible devices found" }
        logger.info { "Compatible devices: [${compatibleDevices.joinToString { it.key.name }}]" }

        val defaultDevice = compatibleDevices.maxBy { it.key.score }
        val physicalDevice = if (deviceName != null) {
            compatibleDevices.firstOrNull { it.key.name == deviceName }
                ?: defaultDevice.also {
                    logger.warn { "Requested device '$deviceName' not found or incompatible, using '${it.key.name}' instead." }
                }
        } else {
            defaultDevice
        }

        logger.info { "Selected device: ${physicalDevice.key.name}" }
        return physicalDevice.key to (physicalDevice.value as VulkanDeviceCompatibility.Compatible).deviceSpec
    }

    context(memScope: MemScope)
    private fun Vulkan.getSupportedExtensions(): Set<VulkanExtension> =
        enumerateInstanceExtensionProperties()
            .map { VulkanExtension(it.extensionName.toKString()) }
            .toSet()

    context(memScope: MemScope)
    private fun Vulkan.getSupportedLayers(): Set<VulkanLayer> =
        enumerateInstanceLayerProperties()
            .map { VulkanLayer(it.layerName.toKString()) }
            .toSet()

    context(memScope: MemScope)
    private fun CommandBuffer.beginRendering(renderTarget: RenderTarget) {
        val attachmentCount = renderTarget.textures.size

        @Suppress("MagicNumber")
        val colorAttachments = memScope.allocArray<VkRenderingAttachmentInfo>(attachmentCount) { index ->
            val texture = renderTarget.textures[index] as VulkanTexture
            sType = VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO
            imageView = texture.imageView.handle
            imageLayout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL
            loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR
            storeOp = VK_ATTACHMENT_STORE_OP_STORE
            clearValue.color.float32[0] = renderTarget.clearValues.color[index].r
            clearValue.color.float32[1] = renderTarget.clearValues.color[index].g
            clearValue.color.float32[2] = renderTarget.clearValues.color[index].b
            clearValue.color.float32[3] = renderTarget.clearValues.color[index].a
        }

        val depthStencilAttachment = renderTarget.depthStencil?.let { depthStencil ->
            check(depthStencil is VulkanTexture)
            memScope.alloc<VkRenderingAttachmentInfo> {
                sType = VK_STRUCTURE_TYPE_RENDERING_ATTACHMENT_INFO
                imageView = depthStencil.imageView.handle
                imageLayout = VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL
                loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR
                storeOp = VK_ATTACHMENT_STORE_OP_STORE
                clearValue.depthStencil.depth = renderTarget.clearValues.depth
                clearValue.depthStencil.stencil = renderTarget.clearValues.stencil
            }
        }

        beginRendering {
            layerCount = 1u
            colorAttachmentCount = attachmentCount.toUInt()
            pColorAttachments = colorAttachments
            pDepthAttachment = depthStencilAttachment?.ptr
            pStencilAttachment = depthStencilAttachment?.ptr
            renderArea.extent.width = renderTarget.extent.width
            renderArea.extent.height = renderTarget.extent.height
        }
    }

    context(memScope: MemScope)
    @Suppress("LongParameterList")
    private fun CommandBuffer.transitionImageLayout(
        texture: VulkanTexture,
        oldLayout: VkImageLayout,
        newLayout: VkImageLayout,
        srcAccessMask: VkAccessFlags2,
        dstAccessMask: VkAccessFlags2,
        srcStage: VkPipelineStageFlags2,
        dstStage: VkPipelineStageFlags2
    ) = imageMemoryBarrier {
        check(texture.layout == oldLayout) { "Expected texture layout to be $oldLayout but was ${texture.layout}" }
        texture.layout = newLayout
        this.srcStageMask = srcStage
        this.srcAccessMask = srcAccessMask
        this.srcQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED
        this.dstStageMask = dstStage
        this.dstAccessMask = dstAccessMask
        this.dstQueueFamilyIndex = VK_QUEUE_FAMILY_IGNORED
        this.oldLayout = oldLayout
        this.newLayout = newLayout
        this.image = texture.image.handle
        this.subresourceRange.aspectMask = texture.aspectMask
        this.subresourceRange.baseArrayLayer = 0u
        this.subresourceRange.layerCount = 1u
        this.subresourceRange.baseMipLevel = 0u
        this.subresourceRange.levelCount = 1u
    }

    companion object {
        private val MIN_API_VERSION = VK_API_VERSION_1_4
    }
}
