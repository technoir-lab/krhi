package io.technoirlab.rhi.mock.geometry

import io.technoirlab.rhi.core.geometry.IndexBuffer
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.mock.MockBuffer

internal class MockIndexBuffer(
    size: ULong,
    override val indexCount: UInt,
    override val indexType: IndexType,
) : MockBuffer(size),
    IndexBuffer
