package io.technoirlab.rhi.core

import web.html.HTMLCanvasElement

actual class WindowHandle(val canvas: HTMLCanvasElement) {
    actual val extent: Extent2D
        get() = Extent2D(canvas.width.toUInt(), canvas.height.toUInt())
}
