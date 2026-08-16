package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.vulkan.Instance
import io.technoirlab.vulkan.presentation.Surface
import io.technoirlab.vulkan.presentation.createMetalSurface
import kotlinx.cinterop.MemScope

context(memScope: MemScope)
internal actual fun Instance.createSurface(windowHandle: WindowHandle): Surface = createMetalSurface(windowHandle.layer)
