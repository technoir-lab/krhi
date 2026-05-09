package io.technoirlab.rhi.core

import kotlinx.cinterop.useContents
import platform.QuartzCore.CAMetalLayer

actual class WindowHandle(val layer: CAMetalLayer) {
    actual val extent: Extent2D
        get() = layer.frame.useContents {
            Extent2D(
                width = size.width.toUInt(),
                height = size.height.toUInt(),
            )
        }
}
