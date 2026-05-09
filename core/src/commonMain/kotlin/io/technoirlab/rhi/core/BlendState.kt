package io.technoirlab.rhi.core

/**
 * Describes the blending behavior.
 */
data class BlendState(
    /**
     * Defines the blending behavior for the corresponding color target.
     */
    val colorTargets: List<ColorTargetBlendState> = listOf(ColorTargetBlendState()),
)

/**
 * Describes the blending behavior for a color target.
 */
data class ColorTargetBlendState(
    /**
     * Controls whether blending is enabled for the corresponding color target.
     * If blending is not enabled, the source fragment’s color for that target is passed through unmodified.
     */
    val blendEnable: Boolean = false,
    /**
     * Defines the blending behavior of the corresponding color target for color channels.
     */
    val color: BlendComponent = BlendComponent(),
    /**
     * Defines the blending behavior of the corresponding color target for the alpha channel.
     */
    val alpha: BlendComponent = BlendComponent(),
    /**
     * Defines which channels are written to when drawing to this color target.
     */
    val colorWriteMask: UInt = ColorMask.ALL,
)

/**
 * Describes how the color or alpha components of a fragment are blended.
 */
data class BlendComponent(
    /**
     * Defines the operation used to calculate the values written to the target attachment components.
     */
    val blendOp: BlendOp = BlendOp.Add,
    /**
     * Defines the operation to be performed on values from the fragment shader.
     */
    val srcFactor: BlendFactor = BlendFactor.One,
    /**
     * Defines the operation to be performed on values from the target attachment.
     */
    val dstFactor: BlendFactor = BlendFactor.Zero,
)

/**
 * Defines how either a source or destination blend factor is calculated.
 */
enum class BlendFactor {
    /**
     * (0, 0, 0, 0)
     */
    Zero,

    /**
     * (1, 1, 1, 1)
     */
    One,

    /**
     * (src.r, src.g, src.b, src.a)
     */
    SrcColor,

    /**
     * (1 - src.r, 1 - src.g, 1 - src.b, 1 - src.a)
     */
    OneMinusSrcColor,

    /**
     * (src.a, src.a, src.a, src.a)
     */
    SrcAlpha,

    /**
     * (1 - src.a, 1 - src.a, 1 - src.a, 1 - src.a)
     */
    OneMinusSrcAlpha,

    /**
     * (dst.r, dst.g, dst.b, dst.a)
     */
    DstColor,

    /**
     * (1 - dst.r, 1 - dst.g, 1 - dst.b, 1 - dst.a)
     */
    OneMinusDstColor,

    /**
     * (dst.a, dst.a, dst.a, dst.a)
     */
    DstAlpha,

    /**
     * (1 - dst.a, 1 - dst.a, 1 - dst.a, 1 - dst.a)
     */
    OneMinusDstAlpha,

    /**
     * (min(src.a, 1 - dst.a), min(src.a, 1 - dst.a), min(src.a, 1 - dst.a), 1)
     */
    SrcAlphaSaturated,

    /**
     * (const.r, const.g, const.b, const.a)
     */
    ConstantColor,

    /**
     * (1 - const.r, 1 - const.g, 1 - const.b, 1 - const.a)
     */
    OneMinusConstantColor,

    /**
     * (src1.r, src1.g, src1.b, src1.a)
     */
    Src1Color,

    /**
     * (1 - src1.r, 1 - src1.g, 1 - src1.b, 1 - src1.a)
     */
    OneMinusSrc1Color,

    /**
     * (src1.a, src1.a, src1.a, src1.a)
     */
    Src1Alpha,

    /**
     * (1 - src1.a, 1 - src1.a, 1 - src1.a, 1 - src.a)
     */
    OneMinusSrc1Alpha,
}

/**
 * Defines the algorithm used to combine source and destination blend factors.
 */
enum class BlendOp {
    /**
     * src × srcFactor + dst × dstFactor
     */
    Add,

    /**
     * src × srcFactor - dst × dstFactor
     */
    Subtract,

    /**
     * dst × dstFactor - src × srcFactor
     */
    ReverseSubtract,

    /**
     * min(src, dst)
     */
    Min,

    /**
     * max(src, dst)
     */
    Max,
}

/**
 * Defines which channels are written to when drawing to a color target.
 */
object ColorMask {
    /**
     * Specifies that the R value is written to the color target for the appropriate sample.
     * Otherwise, the value in memory is unmodified.
     */
    const val R = 0x00000001u

    /**
     * Specifies that the G value is written to the color target for the appropriate sample.
     * Otherwise, the value in memory is unmodified.
     */
    const val G = 0x00000002u

    /**
     * Specifies that the B value is written to the color target for the appropriate sample.
     * Otherwise, the value in memory is unmodified.
     */
    const val B = 0x00000004u

    /**
     * Specifies that the A value is written to the color target for the appropriate sample.
     * Otherwise, the value in memory is unmodified.
     */
    const val A = 0x00000008u

    /**
     * Specifies that all color values are written to the color target for the appropriate sample.
     */
    const val ALL = 0x0000000Fu
}
