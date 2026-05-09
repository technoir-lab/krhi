package io.technoirlab.rhi.vulkan

import io.github.oshai.kotlinlogging.KotlinLogging
import io.technoirlab.rhi.core.ClearValues
import io.technoirlab.rhi.core.Extent2D
import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.FrameState
import io.technoirlab.rhi.core.RenderTarget
import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.volk.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR
import io.technoirlab.volk.VK_ERROR_OUT_OF_DATE_KHR
import io.technoirlab.volk.VK_FENCE_CREATE_SIGNALED_BIT
import io.technoirlab.volk.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT
import io.technoirlab.volk.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
import io.technoirlab.volk.VK_SAMPLE_COUNT_1_BIT
import io.technoirlab.volk.VK_SHARING_MODE_CONCURRENT
import io.technoirlab.volk.VK_SHARING_MODE_EXCLUSIVE
import io.technoirlab.volk.VK_STRUCTURE_TYPE_COMMAND_BUFFER_SUBMIT_INFO
import io.technoirlab.volk.VK_STRUCTURE_TYPE_SEMAPHORE_SUBMIT_INFO
import io.technoirlab.volk.VK_SUBOPTIMAL_KHR
import io.technoirlab.volk.VK_SUCCESS
import io.technoirlab.volk.VK_TRUE
import io.technoirlab.volk.VkCommandBufferSubmitInfo
import io.technoirlab.volk.VkExtent2D
import io.technoirlab.volk.VkSemaphoreSubmitInfo
import io.technoirlab.volk.VkSurfaceCapabilitiesKHR
import io.technoirlab.vulkan.CommandBuffer
import io.technoirlab.vulkan.CommandPool
import io.technoirlab.vulkan.Device
import io.technoirlab.vulkan.Fence
import io.technoirlab.vulkan.Semaphore
import io.technoirlab.vulkan.Surface
import io.technoirlab.vulkan.Swapchain
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlin.time.Duration.Companion.seconds

@Suppress("LongParameterList")
internal class VulkanSwapChain(
    private val device: VulkanDevice,
    private val physicalDevice: VulkanPhysicalDevice,
    private val surface: Surface,
    private val window: WindowHandle,
    private val spec: VulkanSwapChainSpec,
) : RenderTarget {

    private val logger = KotlinLogging.logger("VulkanRenderer")
    private var swapChain: Swapchain
    private var textureStates: List<VulkanTextureState>
    private val frameInFlightState: List<VulkanFrameInFlightState>
    private var frameIndex = 0
    private var currentTextureIndex = -1

    override val arraySize: Int get() = 1
    override val colorFormat: Format get() = spec.format
    override val extent: Extent2D get() = textureStates.first().texture.extent
    override val textures: List<VulkanTexture> get() = listOf(textureStates[currentTextureIndex].texture)
    override var depthStencil: VulkanTexture?
    override val clearValues: ClearValues get() = spec.clearValues

    init {
        memScoped {
            val surfaceCapabilities = physicalDevice.device.getSurfaceCapabilities(surface)
            val extent = chooseTextureExtent(surfaceCapabilities)
            swapChain = createSwapChain(surface, spec, extent)
            textureStates = createImages(device.device, swapChain, extent, spec.format)
            depthStencil = spec.depthStencilFormat?.let { device.createDepthStencilBuffer(extent, spec.depthStencilFormat) }

            frameInFlightState = List(NUM_FRAMES_IN_FLIGHT) {
                VulkanFrameInFlightState(
                    commandPool = device.graphicsCommandPool,
                    commandBuffer = device.graphicsCommandPool.allocateCommandBuffers(1).single(),
                    acquireSemaphore = device.device.createSemaphore(),
                    submitFence = device.device.createFence { flags = VK_FENCE_CREATE_SIGNALED_BIT },
                )
            }
        }
    }

    override fun close() {
        frameInFlightState.forEach { it.close() }
        textureStates.forEach { it.close() }
        depthStencil?.close()
        swapChain.close()
    }

    context(memScope: MemScope)
    fun reset() {
        frameInFlightState.forEach {
            it.submitFence.wait(1.seconds)
            it.commandBuffer.reset()
        }
    }

    context(memScope: MemScope)
    fun acquireNextTexture(): VulkanFrameState {
        val frameInFlightState = frameInFlightState[frameIndex]
        val frameFence = frameInFlightState.submitFence
        val acquireSemaphore = frameInFlightState.acquireSemaphore

        frameFence.wait()
        frameFence.reset()

        var recreate = false
        var result = swapChain.acquireNextImage(acquireSemaphore)
        if (result.status == VK_ERROR_OUT_OF_DATE_KHR) {
            logger.info { "Swap chain is out of date, recreating" }
            recreate()
            result = swapChain.acquireNextImage(acquireSemaphore)
            if (result.status != VK_SUCCESS) {
                logger.warn { "Immediate acquire failed after swap chain recreation" }
            }
        }
        if (result.status == VK_SUBOPTIMAL_KHR) {
            logger.info { "Swap chain is suboptimal, recreating after this frame" }
            recreate = true
        }

        val textureIndex = result.payload
        currentTextureIndex = textureIndex.toInt()
        val textureState = textureStates[currentTextureIndex]
        return VulkanFrameState(
            textureIndex = textureIndex,
            texture = textureState.texture,
            depthStencil = depthStencil,
            commandBuffer = frameInFlightState.commandBuffer,
            acquireSemaphore = acquireSemaphore,
            submitSemaphore = textureState.submitSemaphore,
            submitFence = frameFence,
            recreate = recreate,
        )
    }

    context(memScope: MemScope)
    fun submit(frameState: VulkanFrameState) {
        val commandBufferInfo = memScope.alloc<VkCommandBufferSubmitInfo> {
            sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_SUBMIT_INFO
            commandBuffer = frameState.commandBuffer.handle
        }
        val waitSemaphoreInfo = memScope.alloc<VkSemaphoreSubmitInfo> {
            sType = VK_STRUCTURE_TYPE_SEMAPHORE_SUBMIT_INFO
            semaphore = frameState.acquireSemaphore.handle
            stageMask = VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
        }
        val signalSemaphoreInfo = memScope.alloc<VkSemaphoreSubmitInfo> {
            sType = VK_STRUCTURE_TYPE_SEMAPHORE_SUBMIT_INFO
            semaphore = frameState.submitSemaphore.handle
            stageMask = VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT
        }
        device.graphicsQueue.submit(frameState.submitFence) {
            commandBufferInfoCount = 1u
            pCommandBufferInfos = commandBufferInfo.ptr
            waitSemaphoreInfoCount = 1u
            pWaitSemaphoreInfos = waitSemaphoreInfo.ptr
            signalSemaphoreInfoCount = 1u
            pSignalSemaphoreInfos = signalSemaphoreInfo.ptr
        }
    }

    context(memScope: MemScope)
    fun present(frameState: FrameState) {
        require(frameState is VulkanFrameState)
        val result = device.presentQueue.present(
            swapChain = swapChain,
            imageIndex = frameState.textureIndex,
            waitSemaphores = listOf(frameState.submitSemaphore),
        )
        if (result == VK_ERROR_OUT_OF_DATE_KHR) {
            logger.info { "Swap chain is out of date, recreating immediately" }
            recreate()
        } else if (result == VK_SUBOPTIMAL_KHR || frameState.recreate) {
            logger.info { "Swap chain is suboptimal, recreating immediately" }
            recreate()
        }

        frameIndex = (frameIndex + 1) % NUM_FRAMES_IN_FLIGHT
    }

    context(memScope: MemScope)
    private fun recreate() {
        device.presentQueue.waitIdle()

        val oldSwapChain = swapChain
        val oldExtent = textureStates.first().texture.extent
        val surfaceCapabilities = physicalDevice.device.getSurfaceCapabilities(surface)
        val textureExtent = chooseTextureExtent(surfaceCapabilities)

        textureStates.forEach { it.close() }
        swapChain = createSwapChain(surface, spec, textureExtent, oldSwapChain)
        textureStates = createImages(device.device, swapChain, textureExtent, spec.format)
        oldSwapChain.close()

        if (textureExtent != oldExtent) {
            depthStencil?.close()
            depthStencil = spec.depthStencilFormat?.let { device.createDepthStencilBuffer(textureExtent, spec.depthStencilFormat) }
        }
    }

    context(memScope: MemScope)
    private fun createSwapChain(surface: Surface, spec: VulkanSwapChainSpec, extent: Extent2D, oldSwapchain: Swapchain? = null): Swapchain {
        val queueFamilyIndices = listOf(device.graphicsQueue.familyIndex, device.presentQueue.familyIndex).distinct()
        return device.device.createSwapchain {
            minImageCount = spec.textureCount
            imageFormat = spec.format.toVkFormat()
            imageColorSpace = spec.colorSpace
            imageExtent.width = extent.width
            imageExtent.height = extent.height
            imageArrayLayers = 1u
            imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT
            preTransform = spec.transform
            compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR
            presentMode = spec.presentMode
            queueFamilyIndexCount = queueFamilyIndices.size.toUInt()
            pQueueFamilyIndices = memScope.allocArrayOf(queueFamilyIndices)
            clipped = VK_TRUE
            imageSharingMode = if (queueFamilyIndices.size == 1) VK_SHARING_MODE_EXCLUSIVE else VK_SHARING_MODE_CONCURRENT
            this.surface = surface.handle
            this.oldSwapchain = oldSwapchain?.handle
        }
    }

    context(memScope: MemScope)
    private fun createImages(device: Device, swapchain: Swapchain, extent: Extent2D, format: Format): List<VulkanTextureState> =
        swapchain.getImages().map { image ->
            val aspectMask = getAspectMask(format)
            val imageView = device.createImageView2D(image, format.toVkFormat(), aspectMask)
            val texture = VulkanTexture(
                format = format,
                extent = extent,
                sampleCount = VK_SAMPLE_COUNT_1_BIT,
                image = image,
                imageView = imageView,
                aspectMask = aspectMask,
            )
            VulkanTextureState(texture, submitSemaphore = device.createSemaphore())
        }

    private fun chooseTextureExtent(surfaceCapabilities: VkSurfaceCapabilitiesKHR): Extent2D =
        if (surfaceCapabilities.currentExtent.width != UInt.MAX_VALUE) {
            surfaceCapabilities.currentExtent.toExtent2D()
        } else {
            val windowExtent = window.extent
            Extent2D(
                width = windowExtent.width.coerceIn(surfaceCapabilities.minImageExtent.width, surfaceCapabilities.maxImageExtent.width),
                height = windowExtent.height.coerceIn(surfaceCapabilities.minImageExtent.height, surfaceCapabilities.maxImageExtent.height),
            )
        }

    private fun VkExtent2D.toExtent2D(): Extent2D = Extent2D(width, height)

    private class VulkanTextureState(
        val texture: VulkanTexture,
        val submitSemaphore: Semaphore,
    ) : AutoCloseable {

        override fun close() {
            submitSemaphore.close()
            texture.close()
        }
    }

    private class VulkanFrameInFlightState(
        private val commandPool: CommandPool,
        val commandBuffer: CommandBuffer,
        val acquireSemaphore: Semaphore,
        val submitFence: Fence,
    ) : AutoCloseable {

        override fun close() {
            acquireSemaphore.close()
            submitFence.close()
            memScoped { commandPool.freeCommandBuffers(listOf(commandBuffer)) }
        }
    }

    companion object {
        private const val NUM_FRAMES_IN_FLIGHT = 2
    }
}
