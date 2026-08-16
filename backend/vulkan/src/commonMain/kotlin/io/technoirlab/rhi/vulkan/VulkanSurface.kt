package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.vulkan.Instance
import io.technoirlab.vulkan.presentation.Surface
import kotlinx.cinterop.MemScope

context(memScope: MemScope)
internal expect fun Instance.createSurface(windowHandle: WindowHandle): Surface
