package io.technoirlab.rhi.memory

import kotlin.test.Test
import kotlin.test.assertEquals

class OffsetAllocationTest {
    @Test
    fun `toString names None`() {
        val text = OffsetAllocation.None.toString()

        assertEquals("OffsetAllocation.None", text)
    }

    @Test
    fun `toString shows the node and generation of an allocation`() {
        val allocation = OffsetAllocation.of(node = 5, generation = 3)

        val text = allocation.toString()

        assertEquals("OffsetAllocation(node=5, generation=3)", text)
    }
}
