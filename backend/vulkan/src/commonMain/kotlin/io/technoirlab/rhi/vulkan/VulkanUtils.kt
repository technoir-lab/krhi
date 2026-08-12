@file:Suppress("NOTHING_TO_INLINE")

package io.technoirlab.rhi.vulkan

import io.technoirlab.rhi.core.BlendFactor
import io.technoirlab.rhi.core.BlendOp
import io.technoirlab.rhi.core.ComparisonFunc
import io.technoirlab.rhi.core.CullMode
import io.technoirlab.rhi.core.FillMode
import io.technoirlab.rhi.core.FrontFace
import io.technoirlab.rhi.core.ShaderType
import io.technoirlab.rhi.core.StencilOp
import io.technoirlab.rhi.core.geometry.IndexType
import io.technoirlab.rhi.core.geometry.PrimitiveType
import io.technoirlab.rhi.core.geometry.VertexAttribute
import io.technoirlab.volk.VK_BLEND_FACTOR_CONSTANT_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_DST_ALPHA
import io.technoirlab.volk.VK_BLEND_FACTOR_DST_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_SRC1_ALPHA
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_SRC1_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA
import io.technoirlab.volk.VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_SRC1_ALPHA
import io.technoirlab.volk.VK_BLEND_FACTOR_SRC1_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_SRC_ALPHA
import io.technoirlab.volk.VK_BLEND_FACTOR_SRC_ALPHA_SATURATE
import io.technoirlab.volk.VK_BLEND_FACTOR_SRC_COLOR
import io.technoirlab.volk.VK_BLEND_FACTOR_ZERO
import io.technoirlab.volk.VK_BLEND_OP_ADD
import io.technoirlab.volk.VK_BLEND_OP_MAX
import io.technoirlab.volk.VK_BLEND_OP_MIN
import io.technoirlab.volk.VK_BLEND_OP_REVERSE_SUBTRACT
import io.technoirlab.volk.VK_BLEND_OP_SUBTRACT
import io.technoirlab.volk.VK_COMPARE_OP_ALWAYS
import io.technoirlab.volk.VK_COMPARE_OP_EQUAL
import io.technoirlab.volk.VK_COMPARE_OP_GREATER
import io.technoirlab.volk.VK_COMPARE_OP_GREATER_OR_EQUAL
import io.technoirlab.volk.VK_COMPARE_OP_LESS
import io.technoirlab.volk.VK_COMPARE_OP_LESS_OR_EQUAL
import io.technoirlab.volk.VK_COMPARE_OP_NEVER
import io.technoirlab.volk.VK_COMPARE_OP_NOT_EQUAL
import io.technoirlab.volk.VK_CULL_MODE_BACK_BIT
import io.technoirlab.volk.VK_CULL_MODE_FRONT_BIT
import io.technoirlab.volk.VK_CULL_MODE_NONE
import io.technoirlab.volk.VK_FORMAT_R32G32B32A32_SFLOAT
import io.technoirlab.volk.VK_FORMAT_R32G32B32_SFLOAT
import io.technoirlab.volk.VK_FORMAT_R32G32_SFLOAT
import io.technoirlab.volk.VK_FORMAT_R32_SFLOAT
import io.technoirlab.volk.VK_FRONT_FACE_CLOCKWISE
import io.technoirlab.volk.VK_FRONT_FACE_COUNTER_CLOCKWISE
import io.technoirlab.volk.VK_INDEX_TYPE_UINT16
import io.technoirlab.volk.VK_INDEX_TYPE_UINT32
import io.technoirlab.volk.VK_POLYGON_MODE_FILL
import io.technoirlab.volk.VK_POLYGON_MODE_LINE
import io.technoirlab.volk.VK_PRIMITIVE_TOPOLOGY_LINE_LIST
import io.technoirlab.volk.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP
import io.technoirlab.volk.VK_PRIMITIVE_TOPOLOGY_POINT_LIST
import io.technoirlab.volk.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST
import io.technoirlab.volk.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP
import io.technoirlab.volk.VK_SHADER_STAGE_COMPUTE_BIT
import io.technoirlab.volk.VK_SHADER_STAGE_FRAGMENT_BIT
import io.technoirlab.volk.VK_SHADER_STAGE_GEOMETRY_BIT
import io.technoirlab.volk.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT
import io.technoirlab.volk.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT
import io.technoirlab.volk.VK_SHADER_STAGE_VERTEX_BIT
import io.technoirlab.volk.VK_STENCIL_OP_DECREMENT_AND_CLAMP
import io.technoirlab.volk.VK_STENCIL_OP_DECREMENT_AND_WRAP
import io.technoirlab.volk.VK_STENCIL_OP_INCREMENT_AND_CLAMP
import io.technoirlab.volk.VK_STENCIL_OP_INCREMENT_AND_WRAP
import io.technoirlab.volk.VK_STENCIL_OP_INVERT
import io.technoirlab.volk.VK_STENCIL_OP_KEEP
import io.technoirlab.volk.VK_STENCIL_OP_REPLACE
import io.technoirlab.volk.VK_STENCIL_OP_ZERO
import io.technoirlab.volk.VK_VERSION_MAJOR
import io.technoirlab.volk.VK_VERSION_MINOR
import io.technoirlab.volk.VK_VERSION_PATCH
import io.technoirlab.volk.VkBlendFactor
import io.technoirlab.volk.VkBlendOp
import io.technoirlab.volk.VkColorSpaceKHR
import io.technoirlab.volk.VkCompareOp
import io.technoirlab.volk.VkCullModeFlags
import io.technoirlab.volk.VkExtent2D
import io.technoirlab.volk.VkFormat
import io.technoirlab.volk.VkFrontFace
import io.technoirlab.volk.VkIndexType
import io.technoirlab.volk.VkPolygonMode
import io.technoirlab.volk.VkPresentModeKHR
import io.technoirlab.volk.VkPrimitiveTopology
import io.technoirlab.volk.VkShaderStageFlagBits
import io.technoirlab.volk.VkStencilOp
import io.technoirlab.volk.VkSurfaceTransformFlagBitsKHR
import io.technoirlab.volk.string_VkColorSpaceKHR
import io.technoirlab.volk.string_VkPresentModeKHR
import io.technoirlab.volk.string_VkSurfaceTransformFlagBitsKHR
import kotlinx.cinterop.toKString

internal inline fun vkPresentModeToString(presentMode: VkPresentModeKHR): String =
    string_VkPresentModeKHR(presentMode)!!.toKString().removePrefix("VK_PRESENT_MODE_")

internal inline fun vkColorSpaceToString(colorSpace: VkColorSpaceKHR): String =
    string_VkColorSpaceKHR(colorSpace)!!.toKString().removePrefix("VK_COLOR_SPACE_")

internal inline fun vkSurfaceTransformFlagBitsToString(transform: VkSurfaceTransformFlagBitsKHR): String =
    string_VkSurfaceTransformFlagBitsKHR(transform)!!.toKString().removePrefix("VK_SURFACE_TRANSFORM_")

internal inline fun VkExtent2D.asString(): String = "${width}x$height"

internal inline fun versionToString(version: UInt): String =
    "${VK_VERSION_MAJOR(version)}.${VK_VERSION_MINOR(version)}.${VK_VERSION_PATCH(version)}"

@Suppress("CyclomaticComplexMethod")
internal fun BlendFactor.toVkBlendFactor(): VkBlendFactor = when (this) {
    BlendFactor.Zero -> VK_BLEND_FACTOR_ZERO
    BlendFactor.One -> VK_BLEND_FACTOR_ONE
    BlendFactor.SrcColor -> VK_BLEND_FACTOR_SRC_COLOR
    BlendFactor.OneMinusSrcColor -> VK_BLEND_FACTOR_ONE_MINUS_SRC_COLOR
    BlendFactor.SrcAlpha -> VK_BLEND_FACTOR_SRC_ALPHA
    BlendFactor.OneMinusSrcAlpha -> VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA
    BlendFactor.DstAlpha -> VK_BLEND_FACTOR_DST_ALPHA
    BlendFactor.OneMinusDstAlpha -> VK_BLEND_FACTOR_ONE_MINUS_DST_ALPHA
    BlendFactor.DstColor -> VK_BLEND_FACTOR_DST_COLOR
    BlendFactor.OneMinusDstColor -> VK_BLEND_FACTOR_ONE_MINUS_DST_COLOR
    BlendFactor.SrcAlphaSaturated -> VK_BLEND_FACTOR_SRC_ALPHA_SATURATE
    BlendFactor.ConstantColor -> VK_BLEND_FACTOR_CONSTANT_COLOR
    BlendFactor.OneMinusConstantColor -> VK_BLEND_FACTOR_ONE_MINUS_CONSTANT_COLOR
    BlendFactor.Src1Color -> VK_BLEND_FACTOR_SRC1_COLOR
    BlendFactor.OneMinusSrc1Color -> VK_BLEND_FACTOR_ONE_MINUS_SRC1_COLOR
    BlendFactor.Src1Alpha -> VK_BLEND_FACTOR_SRC1_ALPHA
    BlendFactor.OneMinusSrc1Alpha -> VK_BLEND_FACTOR_ONE_MINUS_SRC1_ALPHA
}

internal fun BlendOp.toVkBlendOp(): VkBlendOp = when (this) {
    BlendOp.Add -> VK_BLEND_OP_ADD
    BlendOp.Subtract -> VK_BLEND_OP_SUBTRACT
    BlendOp.ReverseSubtract -> VK_BLEND_OP_REVERSE_SUBTRACT
    BlendOp.Min -> VK_BLEND_OP_MIN
    BlendOp.Max -> VK_BLEND_OP_MAX
}

internal fun ComparisonFunc.toVkCompareOp(): VkCompareOp = when (this) {
    ComparisonFunc.Never -> VK_COMPARE_OP_NEVER
    ComparisonFunc.Less -> VK_COMPARE_OP_LESS
    ComparisonFunc.Equal -> VK_COMPARE_OP_EQUAL
    ComparisonFunc.LessOrEqual -> VK_COMPARE_OP_LESS_OR_EQUAL
    ComparisonFunc.Greater -> VK_COMPARE_OP_GREATER
    ComparisonFunc.NotEqual -> VK_COMPARE_OP_NOT_EQUAL
    ComparisonFunc.GreaterOrEqual -> VK_COMPARE_OP_GREATER_OR_EQUAL
    ComparisonFunc.Always -> VK_COMPARE_OP_ALWAYS
}

internal fun CullMode.toVkCullMode(): VkCullModeFlags = when (this) {
    CullMode.Back -> VK_CULL_MODE_BACK_BIT
    CullMode.Front -> VK_CULL_MODE_FRONT_BIT
    CullMode.None -> VK_CULL_MODE_NONE
}

internal fun FillMode.toVkPolygonMode(): VkPolygonMode = when (this) {
    FillMode.Solid -> VK_POLYGON_MODE_FILL
    FillMode.Wireframe -> VK_POLYGON_MODE_LINE
}

internal fun FrontFace.toVkFrontFace(): VkFrontFace = when (this) {
    FrontFace.CounterClockwise -> VK_FRONT_FACE_COUNTER_CLOCKWISE
    FrontFace.Clockwise -> VK_FRONT_FACE_CLOCKWISE
}

internal fun IndexType.toVkIndexType(): VkIndexType = when (this) {
    IndexType.Int16 -> VK_INDEX_TYPE_UINT16
    IndexType.Int32 -> VK_INDEX_TYPE_UINT32
}

internal fun PrimitiveType.toVkPrimitiveTopology(): VkPrimitiveTopology = when (this) {
    PrimitiveType.PointList -> VK_PRIMITIVE_TOPOLOGY_POINT_LIST
    PrimitiveType.LineList -> VK_PRIMITIVE_TOPOLOGY_LINE_LIST
    PrimitiveType.LineStrip -> VK_PRIMITIVE_TOPOLOGY_LINE_STRIP
    PrimitiveType.TriangleList -> VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST
    PrimitiveType.TriangleStrip -> VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP
}

internal fun ShaderType.toVkShaderStageFlagBits(): VkShaderStageFlagBits = when (this) {
    ShaderType.Compute -> VK_SHADER_STAGE_COMPUTE_BIT
    ShaderType.Vertex -> VK_SHADER_STAGE_VERTEX_BIT
    ShaderType.Hull -> VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT
    ShaderType.Domain -> VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT
    ShaderType.Geometry -> VK_SHADER_STAGE_GEOMETRY_BIT
    ShaderType.Fragment -> VK_SHADER_STAGE_FRAGMENT_BIT
}

internal fun StencilOp.toVkStencilOp(): VkStencilOp = when (this) {
    StencilOp.Keep -> VK_STENCIL_OP_KEEP
    StencilOp.Zero -> VK_STENCIL_OP_ZERO
    StencilOp.Replace -> VK_STENCIL_OP_REPLACE
    StencilOp.Invert -> VK_STENCIL_OP_INVERT
    StencilOp.IncrementAndClamp -> VK_STENCIL_OP_INCREMENT_AND_CLAMP
    StencilOp.DecrementAndClamp -> VK_STENCIL_OP_DECREMENT_AND_CLAMP
    StencilOp.IncrementAndWrap -> VK_STENCIL_OP_INCREMENT_AND_WRAP
    StencilOp.DecrementAndWrap -> VK_STENCIL_OP_DECREMENT_AND_WRAP
}

internal fun VertexAttribute.Type.toVkFormat(): VkFormat = when (this) {
    VertexAttribute.Type.Float -> VK_FORMAT_R32_SFLOAT
    VertexAttribute.Type.Float2 -> VK_FORMAT_R32G32_SFLOAT
    VertexAttribute.Type.Float3 -> VK_FORMAT_R32G32B32_SFLOAT
    VertexAttribute.Type.Float4 -> VK_FORMAT_R32G32B32A32_SFLOAT
}
