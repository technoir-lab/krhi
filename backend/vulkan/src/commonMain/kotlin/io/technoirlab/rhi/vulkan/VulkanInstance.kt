package io.technoirlab.rhi.vulkan

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT
import io.technoirlab.volk.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT
import io.technoirlab.volk.VK_EXT_DEBUG_UTILS_EXTENSION_NAME
import io.technoirlab.volk.VK_MAKE_VERSION
import io.technoirlab.volk.VkDebugUtilsMessageSeverityFlagBitsEXT
import io.technoirlab.volk.VkDebugUtilsMessageTypeFlagBitsEXT
import io.technoirlab.volk.VkDebugUtilsMessengerCallbackDataEXT
import io.technoirlab.vulkan.ApplicationInfo
import io.technoirlab.vulkan.DebugMessenger
import io.technoirlab.vulkan.Instance
import io.technoirlab.vulkan.Surface
import io.technoirlab.vulkan.Vulkan
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

internal class VulkanInstance(
    private val vulkan: Vulkan,
    private val apiVersion: UInt,
    private val enabledLayers: Set<VulkanLayer>,
    private val enabledExtensions: Set<VulkanExtension>,
) : AutoCloseable {

    private val logger = KotlinLogging.logger("VulkanRenderer")
    private val instance: Instance
    private val debugMessenger: DebugMessenger?

    init {
        logger.info { "Created Vulkan instance. API version ${versionToString(apiVersion)}" }

        if (enabledLayers.isNotEmpty()) {
            logger.info { "Enabled instance layers: [${enabledLayers.joinToString()}]" }
        }
        if (enabledExtensions.isNotEmpty()) {
            logger.info { "Enabled instance extensions: [${enabledExtensions.joinToString()}]" }
        }

        memScoped {
            instance = vulkan.createInstance(
                enabledLayers = enabledLayers.map { it.name },
                enabledExtensions = enabledExtensions.map { it.name },
                applicationInfo = ApplicationInfo(
                    apiVersion = apiVersion,
                    applicationVersion = VK_MAKE_VERSION(1u, 0u, 0u),
                    engineVersion = VK_MAKE_VERSION(1u, 0u, 0u),
                ),
            )

            debugMessenger = if (VulkanExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME) in enabledExtensions) {
                instance.createDebugMessenger(
                    messageSeverity = VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT or
                        VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT or
                        VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT,
                    messageType = VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT or
                        VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT or
                        VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT,
                    callback = DebugMessengerLogger(logger),
                )
            } else {
                null
            }
        }
    }

    override fun close() {
        debugMessenger?.close()
        instance.close()
    }

    context(memScope: MemScope)
    fun createSurface(windowHandle: WindowHandle): Surface = instance.createSurface(windowHandle)

    context(memScope: MemScope)
    fun getPhysicalDevices(): List<VulkanPhysicalDevice> = instance.enumeratePhysicalDevices().map { VulkanPhysicalDevice(it) }.toList()

    private class DebugMessengerLogger(private val logger: KLogger) : DebugMessenger.Callback {
        override fun onEvent(
            messageSeverity: VkDebugUtilsMessageSeverityFlagBitsEXT,
            messageTypes: VkDebugUtilsMessageTypeFlagBitsEXT,
            callbackData: VkDebugUtilsMessengerCallbackDataEXT,
        ) {
            val message = callbackData.pMessage?.toKString() ?: return
            when (messageSeverity) {
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> logger.error { message }
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> logger.warn { message }
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT -> logger.info { message }
                VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT -> logger.debug { message }
            }
        }
    }
}
