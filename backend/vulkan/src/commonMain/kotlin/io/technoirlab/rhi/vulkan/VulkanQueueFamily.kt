package io.technoirlab.rhi.vulkan

import io.technoirlab.volk.VK_QUEUE_COMPUTE_BIT
import io.technoirlab.volk.VK_QUEUE_GRAPHICS_BIT
import io.technoirlab.volk.VkQueueFlags

internal data class VulkanQueueFamily(
    val index: UInt,
    val queueFlags: VkQueueFlags,
    val queueCount: UInt,
) {
    val supportsGraphics: Boolean
        get() = queueFlags and VK_QUEUE_GRAPHICS_BIT != 0u

    val supportsCompute: Boolean
        get() = queueFlags and VK_QUEUE_COMPUTE_BIT != 0u
}
