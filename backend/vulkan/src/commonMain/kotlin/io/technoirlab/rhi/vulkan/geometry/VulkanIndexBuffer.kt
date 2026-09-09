package io.technoirlab.rhi.vulkan.geometry

import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.vulkan.VulkanBuffer
import io.technoirlab.vulkan.memory.DeviceMemory as VkDeviceMemory
import io.technoirlab.vulkan.resource.Buffer as VkBuffer

internal class VulkanIndexBuffer(
    buffer: VkBuffer,
    memory: VkDeviceMemory,
    size: ULong,
    override val indexCount: UInt,
    override val indexType: IndexType,
) : VulkanBuffer(buffer, memory, size),
    IndexBuffer
