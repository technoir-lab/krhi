package io.technoirlab.rhi.vulkan

internal sealed class VulkanDeviceCompatibility {
    abstract val description: String

    data class Compatible(val deviceSpec: VulkanDeviceSpec) : VulkanDeviceCompatibility() {
        override val description: String get() = "Compatible"
    }

    data class ApiVersionIncompatible(val supportedApiVersion: String) : VulkanDeviceCompatibility() {
        override val description: String get() = "API version $supportedApiVersion"
    }

    data class ExtensionsUnsupported(val missingExtensions: Set<VulkanExtension>) : VulkanDeviceCompatibility() {
        override val description: String get() = "Required extensions unsupported: [${missingExtensions.joinToString()}]"
    }

    data object GraphicsIncompatible : VulkanDeviceCompatibility() {
        override val description: String get() = "Graphics unsupported"
    }

    data object PresentationIncompatible : VulkanDeviceCompatibility() {
        override val description: String get() = "Presentation unsupported"
    }

    data object FeaturesUnsupported : VulkanDeviceCompatibility() {
        override val description: String get() = "Required features unsupported"
    }
}
