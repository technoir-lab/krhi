package io.technoirlab.rhi.vulkan

import io.technoirlab.volk.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT
import io.technoirlab.volk.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT
import io.technoirlab.volk.VkMemoryPropertyFlags
import io.technoirlab.vulkan.memory.MemoryProperties
import kotlinx.cinterop.MemScope
import io.technoirlab.vulkan.Device as VkDevice
import io.technoirlab.vulkan.PhysicalDevice as VkPhysicalDevice
import io.technoirlab.vulkan.image.Image as VkImage
import io.technoirlab.vulkan.memory.DeviceMemory as VkDeviceMemory
import io.technoirlab.vulkan.resource.Buffer as VkBuffer

internal class VulkanMemoryManager(
    private val device: VkDevice,
    private val physicalDevice: VkPhysicalDevice,
) {
    context(memScope: MemScope)
    fun allocateBufferMemory(buffer: VkBuffer, flags: VkMemoryPropertyFlags = VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT): VkDeviceMemory {
        val deviceMemoryProperties = physicalDevice.getMemoryProperties()
        val memoryRequirements = buffer.getMemoryRequirements()
        val memory = device.allocateMemory {
            allocationSize = memoryRequirements.size
            memoryTypeIndex = findMemoryType(deviceMemoryProperties, memoryRequirements.memoryTypeBits, flags)
        }
        buffer.bindMemory(memory)
        return memory
    }

    context(memScope: MemScope)
    fun allocateImageMemory(image: VkImage, flags: VkMemoryPropertyFlags = VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT): VkDeviceMemory {
        val deviceMemoryProperties = physicalDevice.getMemoryProperties()
        val memoryRequirements = image.getMemoryRequirements()
        val memory = device.allocateMemory {
            allocationSize = memoryRequirements.size
            memoryTypeIndex = findMemoryType(deviceMemoryProperties, memoryRequirements.memoryTypeBits, flags)
        }
        image.bindMemory(memory)
        return memory
    }

    private fun findMemoryType(memoryProperties: MemoryProperties, memoryTypeBits: UInt, flags: VkMemoryPropertyFlags): UInt {
        var index = UInt.MAX_VALUE
        for (i in memoryProperties.memoryTypes.indices) {
            if ((memoryTypeBits and (1u shl i)) != 0u &&
                (memoryProperties.memoryTypes[i].propertyFlags and flags) == flags
            ) {
                index = i.toUInt()
                break
            }
        }
        check(index != UInt.MAX_VALUE) { "Unable to find suitable memory type" }
        return index
    }
}
