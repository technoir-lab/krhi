package io.technoirlab.rhi.mock

import io.technoirlab.rhi.core.Buffer
import kotlinx.io.Source

internal abstract class MockBuffer(override val size: ULong) : Buffer {
    override fun updateData(source: Source, size: ULong, offset: ULong) = Unit

    override fun close() = Unit
}
