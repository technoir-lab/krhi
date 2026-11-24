package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.Buffer
import kotlinx.cinterop.memScoped
import kotlinx.io.Source
import io.technoirlab.vulkan.buffer.Buffer as VkBuffer
import io.technoirlab.vulkan.memory.DeviceMemory as VkDeviceMemory

internal abstract class VulkanBuffer(
    internal val buffer: VkBuffer,
    private val memory: VkDeviceMemory,
    override val size: ULong,
) : Buffer {
    override fun updateData(source: Source, size: ULong, offset: ULong) = memScoped {
        memory.copyFrom(source, size, offset)
    }

    override fun close() {
        memory.close()
        buffer.close()
    }
}
