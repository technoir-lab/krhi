package io.technoirlab.rhi.vulkan

import io.github.oshai.kotlinlogging.KotlinLogging
import io.technoirlab.rhi.core.BlendState
import io.technoirlab.rhi.core.ColorTargetBlendState
import io.technoirlab.rhi.core.DepthStencilState
import io.technoirlab.rhi.core.Device
import io.technoirlab.rhi.core.Extent2D
import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.GraphicsState
import io.technoirlab.rhi.core.RasterState
import io.technoirlab.rhi.core.RenderTarget
import io.technoirlab.rhi.core.Shader
import io.technoirlab.rhi.core.ShaderType
import io.technoirlab.rhi.core.StencilOpState
import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.rhi.core.config.RendererConfig
import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexBuffer
import io.technoirlab.rhi.core.geometry.VertexLayout
import io.technoirlab.rhi.core.geometry.sizeInBytes
import io.technoirlab.rhi.core.hasStencilComponent
import io.technoirlab.rhi.vulkan.geometry.VulkanIndexBuffer
import io.technoirlab.rhi.vulkan.geometry.VulkanVertexBuffer
import io.technoirlab.volk.VK_BUFFER_USAGE_INDEX_BUFFER_BIT
import io.technoirlab.volk.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
import io.technoirlab.volk.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT
import io.technoirlab.volk.VK_DYNAMIC_STATE_CULL_MODE
import io.technoirlab.volk.VK_DYNAMIC_STATE_FRONT_FACE
import io.technoirlab.volk.VK_DYNAMIC_STATE_PRIMITIVE_TOPOLOGY
import io.technoirlab.volk.VK_DYNAMIC_STATE_SCISSOR_WITH_COUNT
import io.technoirlab.volk.VK_DYNAMIC_STATE_VIEWPORT_WITH_COUNT
import io.technoirlab.volk.VK_FALSE
import io.technoirlab.volk.VK_IMAGE_LAYOUT_UNDEFINED
import io.technoirlab.volk.VK_IMAGE_TILING_OPTIMAL
import io.technoirlab.volk.VK_IMAGE_TYPE_2D
import io.technoirlab.volk.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT
import io.technoirlab.volk.VK_SAMPLE_COUNT_1_BIT
import io.technoirlab.volk.VK_SHADER_STAGE_VERTEX_BIT
import io.technoirlab.volk.VK_SHARING_MODE_EXCLUSIVE
import io.technoirlab.volk.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO
import io.technoirlab.volk.VK_TRUE
import io.technoirlab.volk.VK_VERTEX_INPUT_RATE_VERTEX
import io.technoirlab.volk.VkDeviceQueueCreateInfo
import io.technoirlab.volk.VkFormat
import io.technoirlab.volk.VkFormatVar
import io.technoirlab.volk.VkImageUsageFlags
import io.technoirlab.volk.VkPipelineColorBlendAttachmentState
import io.technoirlab.volk.VkPushConstantRange
import io.technoirlab.volk.VkStencilOpState
import io.technoirlab.volk.VkVertexInputAttributeDescription
import io.technoirlab.volk.VkVertexInputBindingDescription
import io.technoirlab.vulkan.CommandPool
import io.technoirlab.vulkan.Image
import io.technoirlab.vulkan.PhysicalDevice
import io.technoirlab.vulkan.PipelineCache
import io.technoirlab.vulkan.Queue
import io.technoirlab.vulkan.Surface
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCStringArray
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.io.Source
import kotlinx.io.readByteArray
import io.technoirlab.vulkan.Device as VkDevice

internal class VulkanDevice(
    private val physicalDevice: VulkanPhysicalDevice,
    deviceSpec: VulkanDeviceSpec,
    private val enabledExtensions: Set<VulkanExtension>,
) : Device {

    private val logger = KotlinLogging.logger("VulkanRenderer")

    val device: VkDevice
    val graphicsQueue: Queue
    val graphicsCommandPool: CommandPool
    val presentQueue: Queue
    val computeQueue: Queue?
    val computeCommandPool: CommandPool?
    val memoryManager: VulkanMemoryManager

    private val pipelineCache: PipelineCache

    init {
        val queueFamilyIndices = listOfNotNull(
            deviceSpec.graphicsQueueFamilyIndex,
            deviceSpec.presentationQueueFamilyIndex,
            deviceSpec.computeQueueFamilyIndex,
        ).distinct()
        memScoped {
            device = physicalDevice.device.createDevice(queueFamilyIndices)
            if (deviceSpec.presentationQueueFamilyIndex != deviceSpec.graphicsQueueFamilyIndex) {
                logger.info { "Creating graphics queue" }
                graphicsQueue = device.getQueue(deviceSpec.graphicsQueueFamilyIndex)
                logger.info { "Creating presentation queue" }
                presentQueue = device.getQueue(deviceSpec.presentationQueueFamilyIndex)
            } else {
                logger.info { "Creating graphics and presentation queue" }
                graphicsQueue = device.getQueue(deviceSpec.graphicsQueueFamilyIndex)
                presentQueue = graphicsQueue
            }
            graphicsCommandPool = device.createCommandPool(
                queueFamilyIndex = deviceSpec.graphicsQueueFamilyIndex,
                flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT,
            )
            if (deviceSpec.computeQueueFamilyIndex != null) {
                logger.info { "Creating compute queue" }
                computeQueue = device.getQueue(deviceSpec.computeQueueFamilyIndex)
                computeCommandPool = device.createCommandPool(
                    queueFamilyIndex = deviceSpec.computeQueueFamilyIndex,
                    flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT,
                )
            } else {
                computeQueue = null
                computeCommandPool = null
            }

            pipelineCache = device.createPipelineCache()
        }
        memoryManager = VulkanMemoryManager(device, physicalDevice.device)

        if (enabledExtensions.isNotEmpty()) {
            logger.info { "Enabled device extensions: [${enabledExtensions.joinToString()}]" }
        }
    }

    override fun close() {
        pipelineCache.close()
        computeCommandPool?.close()
        computeQueue?.close()
        graphicsCommandPool.close()
        graphicsQueue.close()
        device.close()
    }

    override fun createDepthStencilBuffer(extent: Extent2D, format: Format): VulkanTexture =
        memScoped {
            logger.info { "Creating depth stencil buffer $extent $format" }

            val sampleCount = VK_SAMPLE_COUNT_1_BIT
            val vkFormat = format.toVkFormat()
            val image = device.createImage2D(vkFormat, extent, sampleCount, VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
            val memory = memoryManager.allocateImageMemory(image)
            val aspectMask = getAspectMask(format)
            val imageView = device.createImageView2D(image, vkFormat, aspectMask)
            VulkanTexture(
                format = format,
                extent = extent,
                sampleCount = sampleCount,
                image = image,
                imageView = imageView,
                aspectMask = aspectMask,
                memory = memory,
            )
        }

    override fun createVertexBuffer(source: Source, vertexCount: UInt, vertexLayout: VertexLayout): VertexBuffer =
        memScoped {
            val bufferSize = vertexCount.toULong() * vertexLayout.vertexSize
            val buffer = device.createBuffer {
                usage = VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
                size = bufferSize
            }
            val memory = memoryManager.allocateBufferMemory(buffer)
            memory.copyData(source, buffer.size)
            VulkanVertexBuffer(buffer, memory, bufferSize, vertexCount, vertexLayout)
        }

    override fun createIndexBuffer(source: Source, indexCount: UInt, indexType: IndexType): IndexBuffer =
        memScoped {
            val bufferSize = indexCount.toULong() * indexType.sizeInBytes
            val buffer = device.createBuffer {
                usage = VK_BUFFER_USAGE_INDEX_BUFFER_BIT
                size = bufferSize
            }
            val memory = memoryManager.allocateBufferMemory(buffer)
            memory.copyData(source, buffer.size)
            VulkanIndexBuffer(buffer, memory, bufferSize, indexCount, indexType)
        }

    override fun createShader(type: ShaderType, source: Source, entryPoint: String): Shader =
        memScoped {
            val code = source.readByteArray()
            val shader = code.usePinned { pinned ->
                device.createShaderModule {
                    pCode = pinned.addressOf(0).reinterpret()
                    codeSize = code.size.toULong()
                }
            }
            return VulkanShader(type, entryPoint, shader)
        }

    context(memScope: MemScope)
    fun createSwapChain(surface: Surface, window: WindowHandle, config: RendererConfig): VulkanSwapChain {
        val swapChainSpec = physicalDevice.getSwapChainSpec(surface, config)
        logger.info { "Creating swap chain: $swapChainSpec" }
        return VulkanSwapChain(this, physicalDevice, surface, window, swapChainSpec)
    }

    @Suppress("LongMethod")
    override fun createGraphicsState(
        renderTarget: RenderTarget,
        vertexBuffer: VertexBuffer,
        indexBuffer: IndexBuffer,
        primitiveType: PrimitiveType,
        vertexShader: Shader,
        fragmentShader: Shader,
        rasterState: RasterState,
        blendState: BlendState,
        depthStencilState: DepthStencilState,
        pushConstants: ByteArray?,
    ): GraphicsState =
        memScoped {
            require(vertexBuffer is VulkanVertexBuffer)
            require(indexBuffer is VulkanIndexBuffer)
            require(vertexShader is VulkanShader)
            require(fragmentShader is VulkanShader)

            val vertexInputBindingDescription = alloc<VkVertexInputBindingDescription> {
                binding = 0u
                stride = vertexBuffer.vertexLayout.vertexSize
                inputRate = VK_VERTEX_INPUT_RATE_VERTEX
            }

            val vertexInputAttributes = allocArray<VkVertexInputAttributeDescription>(vertexBuffer.vertexLayout.attributes.size) {
                val vertexAttribute = vertexBuffer.vertexLayout.attributes[it]
                binding = vertexInputBindingDescription.binding
                location = it.toUInt()
                format = vertexAttribute.type.toVkFormat()
                offset = vertexAttribute.offset
            }

            val dynamicStates = listOf(
                VK_DYNAMIC_STATE_CULL_MODE,
                VK_DYNAMIC_STATE_FRONT_FACE,
                VK_DYNAMIC_STATE_PRIMITIVE_TOPOLOGY,
                VK_DYNAMIC_STATE_SCISSOR_WITH_COUNT,
                VK_DYNAMIC_STATE_VIEWPORT_WITH_COUNT,
            )

            val ranges = pushConstants?.let {
                memScope.alloc<VkPushConstantRange> {
                    stageFlags = VK_SHADER_STAGE_VERTEX_BIT
                    size = it.size.toUInt()
                    offset = 0u
                }
            }
            val pipelineLayout = device.createPipelineLayout {
                pushConstantRangeCount = if (pushConstants != null) 1u else 0u
                pPushConstantRanges = ranges?.ptr
            }
            val colorAttachmentFormats = allocArray<VkFormatVar>(renderTarget.arraySize) {
                value = renderTarget.colorFormat.toVkFormat()
            }
            val blendAttachmentState = allocArray<VkPipelineColorBlendAttachmentState>(renderTarget.arraySize) {
                setFrom(blendState.colorTargets[it])
            }
            val pipeline = device.createGraphicsPipeline(
                layout = pipelineLayout,
                stageCount = 2u,
                stages = { index ->
                    val shader = if (index == 0u) vertexShader else fragmentShader
                    stage = shader.type.toVkShaderStageFlagBits()
                    module = shader.shader.handle
                    pName = shader.entryPoint.cstr.ptr
                },
                vertexInputState = {
                    vertexBindingDescriptionCount = 1u
                    pVertexBindingDescriptions = vertexInputBindingDescription.ptr
                    vertexAttributeDescriptionCount = vertexBuffer.vertexLayout.attributes.size.toUInt()
                    pVertexAttributeDescriptions = vertexInputAttributes
                },
                inputAssemblyState = {
                    topology = primitiveType.toVkPrimitiveTopology()
                },
                rasterizationState = {
                    cullMode = rasterState.cullMode.toVkCullMode()
                    frontFace = rasterState.frontFace.toVkFrontFace()
                    polygonMode = rasterState.fillMode.toVkPolygonMode()
                    lineWidth = 1.0f
                },
                multisampleState = {
                    rasterizationSamples = VK_SAMPLE_COUNT_1_BIT
                },
                depthStencilState = {
                    if (renderTarget.depthStencil != null) {
                        depthTestEnable = if (depthStencilState.depthTestEnable) VK_TRUE else VK_FALSE
                        depthWriteEnable = if (depthStencilState.depthWriteEnable) VK_TRUE else VK_FALSE
                        depthCompareOp = depthStencilState.depthCompare.toVkCompareOp()
                        stencilTestEnable = if (depthStencilState.stencilEnable) VK_TRUE else VK_FALSE
                        back.set(depthStencilState, depthStencilState.stencilBack)
                        front.set(depthStencilState, depthStencilState.stencilFront)
                    }
                },
                dynamicState = {
                    dynamicStateCount = dynamicStates.size.toUInt()
                    pDynamicStates = allocArrayOf(dynamicStates)
                },
                renderingCreateInfo = {
                    colorAttachmentCount = renderTarget.arraySize.toUInt()
                    pColorAttachmentFormats = colorAttachmentFormats
                    renderTarget.depthStencil?.let {
                        depthAttachmentFormat = it.format.toVkFormat()
                        if (it.format.hasStencilComponent) {
                            stencilAttachmentFormat = it.format.toVkFormat()
                        }
                    }
                },
                colorBlendState = {
                    attachmentCount = renderTarget.arraySize.toUInt()
                    pAttachments = blendAttachmentState
                },
                cache = pipelineCache,
            )
            return VulkanGraphicsState(
                vertexBuffer = vertexBuffer,
                indexBuffer = indexBuffer,
                primitiveType = primitiveType,
                vertexShader = vertexShader,
                fragmentShader = fragmentShader,
                pipeline = pipeline,
                pipelineLayout = pipelineLayout,
                rasterState = rasterState,
                blendState = blendState,
                depthStencilState = depthStencilState,
                pushConstants = pushConstants,
            )
        }

    context(memScope: MemScope)
    private fun PhysicalDevice.createDevice(queueFamilyIndices: List<UInt>): VkDevice {
        val queueCreateInfos = memScope.allocArray<VkDeviceQueueCreateInfo>(queueFamilyIndices.size) { index ->
            sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO
            queueCount = 1u
            pQueuePriorities = memScope.allocArrayOf(1.0f)
            queueFamilyIndex = queueFamilyIndices[index]
        }
        val enabledExtensionNames = enabledExtensions.map { it.name }.toCStringArray(memScope)
        return createDevice(
            createInfo = {
                queueCreateInfoCount = queueFamilyIndices.size.toUInt()
                pQueueCreateInfos = queueCreateInfos
                enabledExtensionCount = enabledExtensions.size.toUInt()
                ppEnabledExtensionNames = enabledExtensionNames
            },
            features13 = {
                dynamicRendering = VK_TRUE
                synchronization2 = VK_TRUE
            },
        )
    }

    context(memScope: MemScope)
    private fun VkDevice.createImage2D(format: VkFormat, extent: Extent2D, sampleCount: UInt, usage: VkImageUsageFlags): Image =
        createImage {
            imageType = VK_IMAGE_TYPE_2D
            this.format = format
            this.extent.width = extent.width
            this.extent.height = extent.height
            this.extent.depth = 1u
            this.usage = usage
            mipLevels = 1u
            arrayLayers = 1u
            samples = sampleCount
            tiling = VK_IMAGE_TILING_OPTIMAL
            sharingMode = VK_SHARING_MODE_EXCLUSIVE
            initialLayout = VK_IMAGE_LAYOUT_UNDEFINED
        }

    private fun VkStencilOpState.set(depthStencilState: DepthStencilState, stencilOpState: StencilOpState) {
        failOp = stencilOpState.failOp.toVkStencilOp()
        passOp = stencilOpState.passOp.toVkStencilOp()
        depthFailOp = stencilOpState.depthFailOp.toVkStencilOp()
        compareOp = stencilOpState.stencilFunc.toVkCompareOp()
        compareMask = depthStencilState.stencilReadMask.toUInt()
        writeMask = depthStencilState.stencilWriteMask.toUInt()
        reference = depthStencilState.stencilRefValue.toUInt()
    }

    private fun VkPipelineColorBlendAttachmentState.setFrom(blendState: ColorTargetBlendState) {
        blendEnable = if (blendState.blendEnable) VK_TRUE else VK_FALSE
        colorWriteMask = blendState.colorWriteMask

        colorBlendOp = blendState.color.blendOp.toVkBlendOp()
        srcColorBlendFactor = blendState.color.srcFactor.toVkBlendFactor()
        dstColorBlendFactor = blendState.color.dstFactor.toVkBlendFactor()

        alphaBlendOp = blendState.alpha.blendOp.toVkBlendOp()
        srcAlphaBlendFactor = blendState.alpha.srcFactor.toVkBlendFactor()
        dstAlphaBlendFactor = blendState.alpha.dstFactor.toVkBlendFactor()
    }
}
