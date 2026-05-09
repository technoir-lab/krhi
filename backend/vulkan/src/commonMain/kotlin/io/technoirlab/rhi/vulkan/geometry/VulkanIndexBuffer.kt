package io.technoirlab.rhi.vulkan.geometry

import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.vulkan.VulkanBuffer
import io.technoirlab.vulkan.Buffer as VkBuffer
import io.technoirlab.vulkan.DeviceMemory as VkDeviceMemory

internal class VulkanIndexBuffer(
    buffer: VkBuffer,
    memory: VkDeviceMemory,
    size: ULong,
    override val indexCount: UInt,
    override val indexType: IndexType,
) : VulkanBuffer(buffer, memory, size),
    IndexBuffer
