package io.technoirlab.rhi.vulkan

import io.github.oshai.kotlinlogging.KotlinLogging
import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.config.RendererConfig
import io.technoirlab.rhi.core.config.SyncMode
import io.technoirlab.volk.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR
import io.technoirlab.volk.VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT
import io.technoirlab.volk.VK_IMAGE_TILING_LINEAR
import io.technoirlab.volk.VK_IMAGE_TILING_OPTIMAL
import io.technoirlab.volk.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU
import io.technoirlab.volk.VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU
import io.technoirlab.volk.VK_PRESENT_MODE_FIFO_KHR
import io.technoirlab.volk.VK_PRESENT_MODE_IMMEDIATE_KHR
import io.technoirlab.volk.VK_PRESENT_MODE_MAILBOX_KHR
import io.technoirlab.volk.VK_TRUE
import io.technoirlab.volk.VkColorSpaceKHR
import io.technoirlab.volk.VkFormat
import io.technoirlab.volk.VkFormatFeatureFlags
import io.technoirlab.volk.VkImageTiling
import io.technoirlab.volk.VkPresentModeKHR
import io.technoirlab.vulkan.PhysicalDevice
import io.technoirlab.vulkan.Surface
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

internal class VulkanPhysicalDevice(val device: PhysicalDevice) {
    private val logger = KotlinLogging.logger("VulkanRenderer")

    val name: String
    val apiVersion: UInt
    val queueFamilies: List<VulkanQueueFamily>
    private val deviceType: UInt

    init {
        memScoped {
            val properties = device.getProperties()
            name = properties.deviceName.toKString()
            apiVersion = properties.apiVersion
            deviceType = properties.deviceType
            queueFamilies = device.getQueueFamilyProperties()
                .mapIndexed { index, properties ->
                    VulkanQueueFamily(
                        index = index.toUInt(),
                        queueFlags = properties.queueFlags,
                        queueCount = properties.queueCount,
                    )
                }
                .toList()
        }
    }

    val score: Int
        @Suppress("MagicNumber")
        get() {
            var score = 0
            if (deviceType == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                score += 200 // Prefer discrete GPUs
            } else if (deviceType == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
                score += 100
            }
            return score
        }

    fun createDevice(deviceSpec: VulkanDeviceSpec, deviceExtensions: Set<VulkanExtension> = emptySet()): VulkanDevice {
        logger.info { "Creating logical device for $name" }
        logger.info {
            "Graphics queue family index: ${deviceSpec.graphicsQueueFamilyIndex}, " +
                "presentation queue family index: ${deviceSpec.presentationQueueFamilyIndex}, " +
                "compute queue family index: ${deviceSpec.computeQueueFamilyIndex ?: "none"}."
        }
        return VulkanDevice(this, deviceSpec, deviceExtensions)
    }

    context(memScope: MemScope)
    fun getSupportedExtensions(): Set<VulkanExtension> =
        device.enumerateDeviceExtensionProperties()
            .map { VulkanExtension(it.extensionName.toKString()) }
            .toSet()

    context(memScope: MemScope)
    fun getSwapChainSpec(surface: Surface, config: RendererConfig): VulkanSwapChainSpec {
        val surfaceFormats = device.getSurfaceFormats(surface)
            .map { it.format to it.colorSpace }
            .toSet()
        logger.info { "Supported surface formats: [${surfaceFormats.joinToString { it.asString() }}]" }
        val surfaceFormat = selectSurfaceFormat(surfaceFormats, config.hdr)

        val presentModes = device.getSurfacePresentModes(surface)
        logger.info { "Supported present modes: [${presentModes.joinToString { vkPresentModeToString(it) }}]" }
        val presentMode = selectPresentMode(config.syncMode, presentModes)

        val surfaceCapabilities = device.getSurfaceCapabilities(surface)
        logger.info {
            "Surface capabilities: minImageCount=${surfaceCapabilities.minImageCount}, " +
                "maxImageCount=${surfaceCapabilities.maxImageCount}, " +
                "minImageExtent=${surfaceCapabilities.minImageExtent.asString()}, " +
                "maxImageExtent=${surfaceCapabilities.maxImageExtent.asString()}, " +
                "maxImageArrayLayers=${surfaceCapabilities.maxImageArrayLayers}, " +
                "currentExtent=${surfaceCapabilities.currentExtent.asString()}, " +
                "currentTransform=${surfaceCapabilities.currentTransform}"
        }

        val desiredTextureCount = if (config.tripleBuffering) 3u else 2u
        val textureCount = if (surfaceCapabilities.maxImageCount > 0u) {
            desiredTextureCount.coerceIn(surfaceCapabilities.minImageCount, surfaceCapabilities.maxImageCount)
        } else {
            desiredTextureCount.coerceAtLeast(surfaceCapabilities.minImageCount)
        }
        val depthStencilFormat = if (config.depthStencilBuffer) chooseDepthStencilFormat() else null
        return VulkanSwapChainSpec(
            textureCount = textureCount,
            format = surfaceFormat.first,
            colorSpace = surfaceFormat.second,
            presentMode = presentMode,
            transform = surfaceCapabilities.currentTransform,
            depthStencilFormat = depthStencilFormat,
        )
    }

    @Suppress("ReturnCount")
    context(memScope: MemScope)
    fun checkCompatibility(surface: Surface, minApiVersion: UInt): VulkanDeviceCompatibility {
        if (apiVersion < minApiVersion) {
            return VulkanDeviceCompatibility.ApiVersionIncompatible(versionToString(apiVersion))
        }
        val graphicsQueueFamilies = queueFamilies.filter { it.supportsGraphics }
        if (graphicsQueueFamilies.isEmpty()) {
            return VulkanDeviceCompatibility.GraphicsIncompatible
        }
        val presentationQueueFamilies = queueFamilies.filter { device.getSurfaceSupport(surface, it.index) }
        if (presentationQueueFamilies.isEmpty()) {
            return VulkanDeviceCompatibility.PresentationIncompatible
        }
        if (!checkFeatureSupport()) {
            return VulkanDeviceCompatibility.FeaturesUnsupported
        }
        val graphicsAndPresentationQueueFamilies = graphicsQueueFamilies.intersect(presentationQueueFamilies)
        val graphicsQueueFamily = graphicsAndPresentationQueueFamilies.firstOrNull() ?: graphicsQueueFamilies.first()
        val presentationQueueFamily = graphicsAndPresentationQueueFamilies.firstOrNull() ?: presentationQueueFamilies.first()
        val computeQueueFamily = queueFamilies.firstOrNull { it.supportsCompute }
        val deviceSpec = VulkanDeviceSpec(
            graphicsQueueFamilyIndex = graphicsQueueFamily.index,
            presentationQueueFamilyIndex = presentationQueueFamily.index,
            computeQueueFamilyIndex = computeQueueFamily?.index,
        )
        return VulkanDeviceCompatibility.Compatible(deviceSpec)
    }

    context(memScope: MemScope)
    private fun checkFeatureSupport(): Boolean {
        val (_, vulkan13Features) = device.getFeatures()
        return vulkan13Features.dynamicRendering == VK_TRUE && vulkan13Features.synchronization2 == VK_TRUE
    }

    private fun selectSurfaceFormat(surfaceFormats: Set<Pair<VkFormat, VkColorSpaceKHR>>, hdr: Boolean): Pair<Format, VkColorSpaceKHR> {
        if (hdr) {
            val format = HDR_FORMATS.firstOrNull { it.first.toVkFormat() to it.second in surfaceFormats }
            if (format != null) {
                return format
            } else {
                logger.warn { "None of the HDR formats are supported, falling back to SDR" }
            }
        }
        return SDR_FORMATS.firstOrNull { it.first.toVkFormat() to it.second in surfaceFormats }
            ?: error("No supported surface formats found")
    }

    private fun selectPresentMode(syncMode: SyncMode, presentModes: Set<VkPresentModeKHR>): VkPresentModeKHR =
        if (syncMode == SyncMode.Immediate && VK_PRESENT_MODE_IMMEDIATE_KHR in presentModes) {
            VK_PRESENT_MODE_IMMEDIATE_KHR
        } else if (VK_PRESENT_MODE_MAILBOX_KHR in presentModes) {
            VK_PRESENT_MODE_MAILBOX_KHR
        } else {
            VK_PRESENT_MODE_FIFO_KHR
        }

    context(memScope: MemScope)
    private fun chooseDepthStencilFormat(): Format =
        DEPTH_STENCIL_FORMATS.firstOrNull {
            isSupportedFormat(it.toVkFormat(), VK_IMAGE_TILING_OPTIMAL, VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT)
        } ?: error("No supported depth stencil format found")

    context(memScope: MemScope)
    private fun isSupportedFormat(format: VkFormat, tiling: VkImageTiling, flags: VkFormatFeatureFlags): Boolean {
        val formatProperties = device.getFormatProperties(format)
        return (tiling == VK_IMAGE_TILING_LINEAR && (formatProperties.formatProperties.linearTilingFeatures and flags) != 0u) ||
            (tiling == VK_IMAGE_TILING_OPTIMAL && (formatProperties.formatProperties.optimalTilingFeatures and flags) != 0u)
    }

    private fun Pair<VkFormat, VkColorSpaceKHR>.asString(): String = "${vkFormatToString(first)} (${vkColorSpaceToString(second)})"

    companion object {
        private val SDR_FORMATS = arrayOf(
            Format.B8G8R8A8 to VK_COLOR_SPACE_SRGB_NONLINEAR_KHR,
        )
        private val HDR_FORMATS = arrayOf(
            Format.A2B10G10R10 to VK_COLOR_SPACE_SRGB_NONLINEAR_KHR,
        )
        private val DEPTH_STENCIL_FORMATS = arrayOf(
            Format.D32_S8,
            Format.D24_S8,
            Format.D16_S8,
        )
    }
}
