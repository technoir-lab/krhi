package io.technoirlab.rhi.core

/**
 * Describes depth-stencil state.
 */
data class DepthStencilState(
    /**
     * Specifies whether depth testing is enabled.
     */
    val depthTestEnable: Boolean = true,
    /**
     * Specifies whether depth writes are enabled when [depthTestEnable] is true.
     * Depth writes are always disabled when [depthTestEnable] is false.
     */
    val depthWriteEnable: Boolean = true,
    /**
     * The comparison operation used to test fragment depths against depth-stencil attachment depth values.
     */
    val depthCompare: ComparisonFunc = ComparisonFunc.Less,
    /**
     * Specifies whether stencil testing is enabled.
     */
    val stencilEnable: Boolean = false,
    /**
     * Bitmask controlling which depth-stencil attachment stencil value bits are read when performing stencil comparison tests.
     */
    val stencilReadMask: UByte = 0xffu,
    /**
     * Bitmask controlling which depth-stencil attachment stencil value bits are written to when performing stencil operations.
     */
    val stencilWriteMask: UByte = 0xffu,
    /**
     * The reference value that is used in the stencil comparison.
     */
    val stencilRefValue: UByte = 0u,
    /**
     * Defines how stencil comparisons and operations are performed for front-facing primitives.
     */
    val stencilFront: StencilOpState = StencilOpState(),
    /**
     * Defines how stencil comparisons and operations are performed for back-facing primitives.
     */
    val stencilBack: StencilOpState = StencilOpState(),
)

/**
 * Describes how stencil comparisons and operations are performed.
 */
data class StencilOpState(
    /**
     * The [StencilOp] performed if the fragment stencil comparison test described by compare fails.
     */
    val failOp: StencilOp = StencilOp.Keep,
    /**
     * The [StencilOp] performed if the fragment depth comparison described by [DepthStencilState.depthCompare] fails.
     */
    val depthFailOp: StencilOp = StencilOp.Keep,
    /**
     * The [StencilOp] performed if the fragment stencil comparison test described by compare passes.
     */
    val passOp: StencilOp = StencilOp.Keep,
    /**
     * The [ComparisonFunc] used when testing the [DepthStencilState.stencilRefValue] value against the fragment’s depth-stencil attachment stencil values.
     */
    val stencilFunc: ComparisonFunc = ComparisonFunc.Always,
)

/**
 * Stencil operations that can be performed during depth-stencil testing.
 */
enum class StencilOp {
    /**
     * Keep the current stencil value.
     */
    Keep,

    /**
     * Set the stencil value to 0.
     */
    Zero,

    /**
     * Set the stencil value to [DepthStencilState.stencilRefValue].
     */
    Replace,

    /**
     * Bitwise-invert the current stencil value.
     */
    Invert,

    /**
     * Increments the current stencil value, clamping to the maximum representable value of the depth-stencil attachment’s stencil aspect.
     */
    IncrementAndClamp,

    /**
     * Decrement the current stencil value, clamping to 0.
     */
    DecrementAndClamp,

    /**
     * Increments the current stencil value, wrapping to zero if the value exceeds the maximum representable value of the depth-stencil attachment’s stencil aspect.
     */
    IncrementAndWrap,

    /**
     * Decrement the current stencil value, wrapping to the maximum representable value of the depth-stencil attachment’s stencil aspect if the value goes below 0.
     */
    DecrementAndWrap,
}

/**
 * Specifies the behavior of a comparison sampler.
 */
enum class ComparisonFunc {
    /**
     * Comparison tests never pass.
     */
    Never,

    /**
     * A provided value passes the comparison test if it is less than the sampled value.
     */
    Less,

    /**
     * A provided value passes the comparison test if it is equal to the sampled value.
     */
    Equal,

    /**
     * A provided value passes the comparison test if it is less than or equal to the sampled value.
     */
    LessOrEqual,

    /**
     *A provided value passes the comparison test if it is greater than the sampled value.
     */
    Greater,

    /**
     * A provided value passes the comparison test if it is not equal to the sampled value.
     */
    NotEqual,

    /**
     * A provided value passes the comparison test if it is greater than or equal to the sampled value.
     */
    GreaterOrEqual,

    /**
     * Comparison tests always pass.
     */
    Always,
}
