package io.technoirlab.rhi.samples.common

import io.technoirlab.rhi.core.Device

/**
 * Base class for samples.
 */
abstract class Sample(protected val device: Device) : AutoCloseable {
    abstract fun run()

    override fun close() {
        device.close()
    }
}
