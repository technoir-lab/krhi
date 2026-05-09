package io.technoirlab.rhi.mock

import io.technoirlab.rhi.core.Extent2D
import io.technoirlab.rhi.core.Format
import io.technoirlab.rhi.core.Texture

internal class MockTexture(
    override val extent: Extent2D,
    override val format: Format,
    override val sampleCount: UInt,
) : Texture {
    override fun close() = Unit
}
