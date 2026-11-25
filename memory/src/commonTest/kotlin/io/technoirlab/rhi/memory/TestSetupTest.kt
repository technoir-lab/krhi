package io.technoirlab.rhi.memory

import kotlin.test.Test
import kotlin.test.assertEquals

class TestSetupTest {
    @Test
    fun `tests run on every target`() {
        val values = listOf(1, 2, 3)

        val sum = values.sum()

        assertEquals(6, sum)
    }
}
