package io.technoirlab.rhi.core

import io.technoirlab.rhi.core.Format.D16
import io.technoirlab.rhi.core.Format.D16_S8
import io.technoirlab.rhi.core.Format.D24
import io.technoirlab.rhi.core.Format.D24_S8
import io.technoirlab.rhi.core.Format.D32
import io.technoirlab.rhi.core.Format.D32_S8

enum class Format {
    B8G8R8A8,
    A2B10G10R10,
    D16,
    D16_S8,
    D24,
    D24_S8,
    D32,
    D32_S8,
}

inline val Format.hasDepthComponent: Boolean
    get() = when (this) {
        D16,
        D16_S8,
        D24,
        D24_S8,
        D32,
        D32_S8,
        -> true

        else -> false
    }

inline val Format.hasStencilComponent: Boolean
    get() = when (this) {
        D16_S8,
        D24_S8,
        D32_S8,
        -> true

        else -> false
    }
