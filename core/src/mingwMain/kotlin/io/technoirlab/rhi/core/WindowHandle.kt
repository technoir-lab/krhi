package io.technoirlab.rhi.core

import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.GetClientRect
import platform.windows.HINSTANCE
import platform.windows.HWND
import platform.windows.RECT

actual class WindowHandle(
    val nativeHandle: HWND,
    val moduleHandle: HINSTANCE,
) {
    actual val extent: Extent2D
        get() = memScoped {
            val rect = alloc<RECT>()
            GetClientRect(nativeHandle, rect.ptr)
            Extent2D(
                width = (rect.right - rect.left).toUInt(),
                height = (rect.bottom - rect.top).toUInt(),
            )
        }
}
