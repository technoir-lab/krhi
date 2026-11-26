package io.technoirlab.rhi.memory

/**
 * A read-only view of contiguous host memory.
 *
 * A span never allocates or frees memory, and nothing can be freed through it. Whoever created the memory keeps it valid
 * for as long as the span is used. A [MutableMemorySpan] can also write, and can be passed wherever a span is expected.
 *
 * Multi-byte values are little-endian on every target, in typed access and in array transfer alike. A JS engine on a
 * big-endian host keeps its arrays in the host's order; array transfer and `useSpan` throw [UnsupportedOperationException]
 * there rather than move multi-byte values in that order.
 *
 * Offsets and sizes count bytes, whatever the type of the value read or written; only the `startIndex` and `endIndex` of an
 * array transfer count elements. They are [Int] in every operation, so a span holds at most [Int.MAX_VALUE] bytes, just under
 * 2 GiB, on every target. Memory larger than that is seen through several spans, which whoever owns the memory makes.
 *
 * Every operation checks its range, on every target and in every build, and throws [IndexOutOfBoundsException] when an
 * element or a range leaves the span. Operations on a zero-length span touch no memory.
 *
 * A span is not synchronised. Reading from several threads at once is safe; a write that overlaps another access is not.
 */
expect open class MemorySpan {
    /**
     * The number of bytes in the span.
     */
    val size: Int

    /**
     * Returns a span over [size] bytes of this one, starting at [offset]. The result reads the same memory.
     *
     * @throws IndexOutOfBoundsException when the range leaves the span.
     */
    open fun slice(offset: Int, size: Int = this.size - offset): MemorySpan

    /**
     * Copies [size] bytes starting at [offset] into [destination], starting at [destinationOffset].
     * The two ranges may overlap.
     *
     * @throws IndexOutOfBoundsException when the range leaves this span or [destination].
     */
    fun copyTo(destination: MutableMemorySpan, destinationOffset: Int = 0, offset: Int = 0, size: Int = this.size - offset)

    /**
     * Returns the byte at [offset].
     */
    fun getByte(offset: Int): Byte

    /**
     * Returns the little-endian [Short] at [offset], which needs no alignment.
     */
    fun getShort(offset: Int): Short

    /**
     * Returns the little-endian [Int] at [offset], which needs no alignment.
     */
    fun getInt(offset: Int): Int

    /**
     * Returns the little-endian [Long] at [offset], which needs no alignment.
     */
    fun getLong(offset: Int): Long

    /**
     * Returns the little-endian [Float] at [offset], which needs no alignment.
     */
    fun getFloat(offset: Int): Float

    /**
     * Returns the little-endian [Double] at [offset], which needs no alignment.
     */
    fun getDouble(offset: Int): Double

    /**
     * Copies bytes starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex] (exclusive).
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun read(destination: ByteArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size)

    /**
     * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun read(destination: ShortArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size)

    /**
     * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun read(destination: IntArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size)

    /**
     * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun read(destination: LongArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size)

    /**
     * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun read(destination: FloatArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size)

    /**
     * Copies little-endian values starting at byte [offset] into [destination] from [startIndex] (inclusive) to [endIndex]
     * (exclusive).
     *
     * @throws IndexOutOfBoundsException when the range leaves the span or the array.
     * @throws IllegalArgumentException when [startIndex] is greater than [endIndex].
     */
    fun read(destination: DoubleArray, offset: Int = 0, startIndex: Int = 0, endIndex: Int = destination.size)
}
