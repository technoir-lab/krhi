package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.Extent2D
import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.Texture
import io.technoirlab.volk.VK_COMPONENT_SWIZZLE_IDENTITY
import io.technoirlab.volk.VK_IMAGE_LAYOUT_UNDEFINED
import io.technoirlab.volk.VK_IMAGE_VIEW_TYPE_2D
import io.technoirlab.volk.VkFormat
import io.technoirlab.volk.VkImageAspectFlags
import io.technoirlab.volk.VkImageLayout
import io.technoirlab.vulkan.Device
import io.technoirlab.vulkan.DeviceMemory
import io.technoirlab.vulkan.Image
import io.technoirlab.vulkan.ImageView
import kotlinx.cinterop.MemScope

@Suppress("LongParameterList")
internal class VulkanTexture(
    override val extent: Extent2D,
    override val format: Format,
    override val sampleCount: UInt,
    val image: Image,
    val imageView: ImageView,
    val aspectMask: VkImageAspectFlags,
    var layout: VkImageLayout = VK_IMAGE_LAYOUT_UNDEFINED,
    private val memory: DeviceMemory? = null,
) : Texture {

    override fun close() {
        imageView.close()
        image.close()
        memory?.close()
    }
}

context(memScope: MemScope)
internal fun Device.createImageView2D(image: Image, format: VkFormat, aspectMask: VkImageAspectFlags): ImageView =
    createImageView {
        viewType = VK_IMAGE_VIEW_TYPE_2D
        components.r = VK_COMPONENT_SWIZZLE_IDENTITY
        components.g = VK_COMPONENT_SWIZZLE_IDENTITY
        components.b = VK_COMPONENT_SWIZZLE_IDENTITY
        components.a = VK_COMPONENT_SWIZZLE_IDENTITY
        subresourceRange.aspectMask = aspectMask
        subresourceRange.baseMipLevel = 0u
        subresourceRange.levelCount = 1u
        subresourceRange.baseArrayLayer = 0u
        subresourceRange.layerCount = 1u
        this.image = image.handle
        this.format = format
    }
