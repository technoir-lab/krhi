package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.vulkan.Instance
import io.technoirlab.vulkan.presentation.Surface
import io.technoirlab.vulkan.presentation.createWin32Surface
import kotlinx.cinterop.MemScope

context(memScope: MemScope)
internal actual fun Instance.createSurface(windowHandle: WindowHandle): Surface = createWin32Surface(
    hinstance = windowHandle.moduleHandle,
    hwnd = windowHandle.nativeHandle,
)
