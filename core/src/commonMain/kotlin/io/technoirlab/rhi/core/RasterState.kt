package io.technoirlab.rhi.core

/**
 * Describes rasterization state.
 */
data class RasterState(
    /**
     * Defines which polygon orientation will be culled, if any.
     */
    val cullMode: CullMode = CullMode.Back,
    /**
     * Defines polygon rasterization mode.
     */
    val fillMode: FillMode = FillMode.Solid,
    /**
     * Defines which polygons are considered front-facing.
     */
    val frontFace: FrontFace = FrontFace.CounterClockwise,
)

/**
 * Defines which polygons will be culled by draw calls.
 */
enum class CullMode {
    /**
     * Back-facing polygons are discarded.
     */
    Back,

    /**
     * Front-facing polygons are discarded.
     */
    Front,

    /**
     * No polygons are discarded.
     */
    None,
}

/**
 * Defines polygon rasterization mode.
 */
enum class FillMode {
    /**
     * Polygons are filled.
     */
    Solid,

    /**
     * Polygon edges are drawn as line segments.
     */
    Wireframe,
}

/**
 * Defines which polygons are considered front-facing.
 */
enum class FrontFace {
    /**
     * Polygons with vertices whose framebuffer coordinates are given in counter-clockwise order are considered front-facing.
     */
    CounterClockwise,

    /**
     * Polygons with vertices whose framebuffer coordinates are given in clockwise order are considered front-facing.
     */
    Clockwise,
}
