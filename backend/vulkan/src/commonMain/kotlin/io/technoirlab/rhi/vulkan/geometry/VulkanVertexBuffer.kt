package io.technoirlab.rhi.vulkan.geometry

import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.rhi.core.geometry.VertexLayout
import io.technoirlab.rhi.vulkan.VulkanBuffer
import io.technoirlab.vulkan.memory.DeviceMemory as VkDeviceMemory
import io.technoirlab.vulkan.resource.Buffer as VkBuffer

internal class VulkanVertexBuffer(
    buffer: VkBuffer,
    memory: VkDeviceMemory,
    size: ULong,
    override val vertexCount: UInt,
    override val vertexLayout: VertexLayout,
) : VulkanBuffer(buffer, memory, size),
    VertexBuffer
