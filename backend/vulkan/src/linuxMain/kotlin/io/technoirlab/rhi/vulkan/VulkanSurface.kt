package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.vulkan.Instance
import io.technoirlab.vulkan.Surface
import io.technoirlab.vulkan.createWaylandSurface
import kotlinx.cinterop.MemScope

context(memScope: MemScope)
internal actual fun Instance.createSurface(windowHandle: WindowHandle): Surface = createWaylandSurface {
    display = TODO()
    surface = TODO()
}
