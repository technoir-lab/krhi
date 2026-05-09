package io.technoirlab.rhi.vulkan

import io.technoirlab.volk.VK_EXT_DEBUG_UTILS_EXTENSION_NAME

internal value class VulkanExtension(val name: String) : Comparable<VulkanExtension> {
    override fun compareTo(other: VulkanExtension): Int = name.compareTo(other.name)

    override fun toString(): String = name
}

internal fun Collection<VulkanExtension>.filter(
    requiredExtensions: Set<VulkanExtension> = emptySet(),
    optionalExtensions: Set<VulkanExtension> = emptySet(),
): Set<VulkanExtension> {
    val missingExtensions = requiredExtensions.minus(this)
    check(missingExtensions.isEmpty()) {
        "Required Vulkan extensions are unsupported: [${missingExtensions.joinToString()}]"
    }
    return requiredExtensions + optionalExtensions.intersect(this)
}

internal expect val REQUIRED_INSTANCE_EXTENSIONS: Set<VulkanExtension>
internal val OPTIONAL_INSTANCE_EXTENSIONS: Set<VulkanExtension> = setOf(
    VulkanExtension(VK_EXT_DEBUG_UTILS_EXTENSION_NAME),
)
internal expect val REQUIRED_DEVICE_EXTENSIONS: Set<VulkanExtension>
internal val OPTIONAL_DEVICE_EXTENSIONS: Set<VulkanExtension> = emptySet()
