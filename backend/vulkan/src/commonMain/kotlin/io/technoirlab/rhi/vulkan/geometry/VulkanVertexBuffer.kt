package io.technoirlab.rhi.vulkan.geometry

import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.rhi.core.geometry.VertexLayout
import io.technoirlab.rhi.vulkan.VulkanBuffer
import io.technoirlab.vulkan.Buffer as VkBuffer
import io.technoirlab.vulkan.DeviceMemory as VkDeviceMemory

internal class VulkanVertexBuffer(
    buffer: VkBuffer,
    memory: VkDeviceMemory,
    size: ULong,
    override val vertexCount: UInt,
    override val vertexLayout: VertexLayout,
) : VulkanBuffer(buffer, memory, size),
    VertexBuffer
