package io.technoirlab.rhi.vulkan

import io.technoirlab.volk.VK_KHR_SURFACE_EXTENSION_NAME
import io.technoirlab.volk.VK_KHR_SWAPCHAIN_EXTENSION_NAME
import io.technoirlab.volk.VK_KHR_WIN32_SURFACE_EXTENSION_NAME

internal actual val REQUIRED_INSTANCE_EXTENSIONS: Set<VulkanExtension> = setOf(
    VulkanExtension(VK_KHR_SURFACE_EXTENSION_NAME),
    VulkanExtension(VK_KHR_WIN32_SURFACE_EXTENSION_NAME),
)

internal actual val REQUIRED_DEVICE_EXTENSIONS: Set<VulkanExtension> = setOf(
    VulkanExtension(VK_KHR_SWAPCHAIN_EXTENSION_NAME),
)
