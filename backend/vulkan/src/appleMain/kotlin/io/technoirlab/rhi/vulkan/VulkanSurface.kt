package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.vulkan.Instance
import io.technoirlab.vulkan.Surface
import io.technoirlab.vulkan.createMetalSurface
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.interpretCPointer
import kotlinx.cinterop.objcPtr

context(memScope: MemScope)
internal actual fun Instance.createSurface(windowHandle: WindowHandle): Surface = createMetalSurface {
    pLayer = interpretCPointer(windowHandle.layer.objcPtr())
}
