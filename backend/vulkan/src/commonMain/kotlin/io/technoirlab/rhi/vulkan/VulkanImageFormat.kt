package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.hasDepthComponent
import io.technoirlab.rhi.core.hasStencilComponent
import io.technoirlab.volk.VK_FORMAT_A2B10G10R10_UNORM_PACK32
import io.technoirlab.volk.VK_FORMAT_B8G8R8A8_UNORM
import io.technoirlab.volk.VK_FORMAT_D16_UNORM
import io.technoirlab.volk.VK_FORMAT_D16_UNORM_S8_UINT
import io.technoirlab.volk.VK_FORMAT_D24_UNORM_S8_UINT
import io.technoirlab.volk.VK_FORMAT_D32_SFLOAT
import io.technoirlab.volk.VK_FORMAT_D32_SFLOAT_S8_UINT
import io.technoirlab.volk.VK_FORMAT_X8_D24_UNORM_PACK32
import io.technoirlab.volk.VK_IMAGE_ASPECT_COLOR_BIT
import io.technoirlab.volk.VK_IMAGE_ASPECT_DEPTH_BIT
import io.technoirlab.volk.VK_IMAGE_ASPECT_STENCIL_BIT
import io.technoirlab.volk.VkFormat
import io.technoirlab.volk.VkImageAspectFlags
import io.technoirlab.volk.string_VkFormat
import kotlinx.cinterop.toKString

internal fun Format.toVkFormat(): VkFormat =
    when (this) {
        Format.B8G8R8A8 -> VK_FORMAT_B8G8R8A8_UNORM
        Format.A2B10G10R10 -> VK_FORMAT_A2B10G10R10_UNORM_PACK32
        Format.D16 -> VK_FORMAT_D16_UNORM
        Format.D16_S8 -> VK_FORMAT_D16_UNORM_S8_UINT
        Format.D24 -> VK_FORMAT_X8_D24_UNORM_PACK32
        Format.D24_S8 -> VK_FORMAT_D24_UNORM_S8_UINT
        Format.D32 -> VK_FORMAT_D32_SFLOAT
        Format.D32_S8 -> VK_FORMAT_D32_SFLOAT_S8_UINT
    }

internal fun getAspectMask(format: Format): VkImageAspectFlags {
    var aspectMask = 0u
    if (format.hasDepthComponent) {
        aspectMask = aspectMask or VK_IMAGE_ASPECT_DEPTH_BIT
    }
    if (format.hasStencilComponent) {
        aspectMask = aspectMask or VK_IMAGE_ASPECT_STENCIL_BIT
    }
    if (aspectMask == 0u) {
        aspectMask = VK_IMAGE_ASPECT_COLOR_BIT
    }
    return aspectMask
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun vkFormatToString(format: VkFormat): String = string_VkFormat(format)!!.toKString().removePrefix("VK_FORMAT_")
