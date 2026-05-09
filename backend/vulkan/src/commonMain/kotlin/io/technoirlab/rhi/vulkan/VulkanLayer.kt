package io.technoirlab.rhi.vulkan

internal value class VulkanLayer(val name: String) : Comparable<VulkanLayer> {
    override fun compareTo(other: VulkanLayer): Int = name.compareTo(other.name)

    override fun toString(): String = name
}

internal fun Collection<VulkanLayer>.filter(
    requiredLayers: Set<VulkanLayer> = emptySet(),
    optionalLayers: Set<VulkanLayer> = emptySet(),
): Set<VulkanLayer> {
    val missingExtensions = requiredLayers.minus(this)
    check(missingExtensions.isEmpty()) {
        "Required Vulkan layers are unsupported: [${missingExtensions.joinToString()}]"
    }
    return requiredLayers + optionalLayers.intersect(this)
}

internal val REQUIRED_LAYERS: Set<VulkanLayer> = emptySet()
internal val OPTIONAL_LAYERS: Set<VulkanLayer> = setOf(
    VulkanLayer("VK_LAYER_KHRONOS_validation"),
)
