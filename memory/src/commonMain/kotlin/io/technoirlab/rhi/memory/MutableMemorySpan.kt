package io.technoirlab.rhi.memory

/**
 * A view of contiguous host memory that can also be written.
 *
 * It follows the rules of [MemorySpan]: it owns nothing, it is little-endian, and it is not synchronised.
 */
expect class MutableMemorySpan : MemorySpan {
    /**
     * Returns a writable span over [size] bytes of this one, starting at [offset]. By default the bytes from [offset] to
     * the end of the span. The result writes the same memory.
     *
     * @throws IndexOutOfBoundsException when the range leaves the span.
     */
    override fun slice(offset: Int, size: Int): MutableMemorySpan

    // The default goes through an extension: Kotlin 2.4.21 crashes on a default that reads the inherited size here.

    /**
     * Sets [size] bytes starting at [offset] to [value]. By default the bytes from [offset] to the end of the span.
     *
     * @throws IndexOutOfBoundsException when the range leaves the span.
     */
    fun fill(value: Byte, offset: Int = 0, size: Int = bytesAfter(offset))

    /**
     * Writes [value] at [offset].
     */
    fun setByte(offset: Int, value: Byte)

    /**
     * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
     */
    fun setShort(offset: Int, value: Short)

    /**
     * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
     */
    fun setInt(offset: Int, value: Int)

    /**
     * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
     */
    fun setLong(offset: Int, value: Long)

    /**
     * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
     */
    fun setFloat(offset: Int, value: Float)

    /**
     * Writes [value] at [offset] in little-endian order. The offset needs no alignment.
     */
    fun setDouble(offset: Int, value: Double)

    /**
     * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span, starting at byte [offset].
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun write(source: ByteArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size)

    /**
     * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
     * starting at byte [offset].
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun write(source: ShortArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size)

    /**
     * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
     * starting at byte [offset].
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun write(source: IntArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size)

    /**
     * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
     * starting at byte [offset].
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun write(source: LongArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size)

    /**
     * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
     * starting at byte [offset].
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun write(source: FloatArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size)

    /**
     * Copies [source] from [startIndex] (inclusive) to [endIndex] (exclusive) into the span as little-endian values,
     * starting at byte [offset].
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun write(source: DoubleArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = source.size)
}
