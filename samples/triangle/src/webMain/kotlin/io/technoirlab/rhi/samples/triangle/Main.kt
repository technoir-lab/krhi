package io.technoirlab.rhi.samples.triangle

import io.technoirlab.rhi.core.WindowHandle
import io.technoirlab.rhi.webgpu.WebGpuDevice
import web.dom.ElementId
import web.dom.document
import web.events.addEventListener
import web.history.PAGE_HIDE
import web.history.PageTransitionEvent
import web.html.HTMLCanvasElement
import web.window.window

suspend fun main() {
    val status = checkNotNull(document.getElementById(ElementId("webgpu-status"))) { "No 'webgpu-status' element found!" }
    try {
        val canvas = checkNotNull(document.getElementById(ElementId("webgpu-canvas"))) { "No 'webgpu-canvas' element found!" }
        val device = WebGpuDevice.create(WindowHandle(canvas as HTMLCanvasElement))
        val sample = TriangleSample(device)
        sample.run()
        window.addEventListener(PageTransitionEvent.PAGE_HIDE, { event ->
            if (!event.persisted) {
                sample.close()
            }
        })
        status.textContent = "WebGPU initialized."
    } catch (exception: Throwable) {
        status.textContent = "WebGPU initialization failed: ${exception.message}"
    }
}
