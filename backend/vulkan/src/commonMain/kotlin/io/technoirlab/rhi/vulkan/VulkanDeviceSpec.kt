package io.technoirlab.rhi.vulkan

internal data class VulkanDeviceSpec(
    val graphicsQueueFamilyIndex: UInt,
    val presentationQueueFamilyIndex: UInt,
    val computeQueueFamilyIndex: UInt?,
)
