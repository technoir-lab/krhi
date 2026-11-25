package io.technoirlab.rhi.memory

/**
 * Returns the number of bytes from [offset] to the end of the span.
 */
internal fun MemorySpan.bytesAfter(offset: Int): Int = size - offset

/**
 * Checks that [size] bytes starting at [offset] lie inside [owner], which has [spanSize] bytes. The owner is named in the
 * message, as in "a span".
 */
internal fun checkSpanRange(offset: Int, size: Int, spanSize: Int, owner: String = "a span") {
    // The subtraction cannot overflow: it is reached only with a size that is not negative.
    if (offset < 0 || size < 0 || offset > spanSize - size) {
        throwRangeOutOfBounds(offset.toLong(), size.toLong(), spanSize.toLong(), owner)
    }
}

/**
 * Throws for a range that lies outside its owner. An offset that is outside by itself is reported alone: a size that
 * defaults to the bytes after the offset is negative then, and is not what the caller got wrong.
 *
 * The values are [Long] so that a caller can report a size or an owner that an [Int] cannot hold.
 */
internal fun throwRangeOutOfBounds(offset: Long, size: Long, spanSize: Long, owner: String = "a span"): Nothing {
    val range = if (offset < 0 || offset > spanSize) "Offset $offset" else "Range of $size bytes at offset $offset"
    throw IndexOutOfBoundsException("$range is outside $owner of $spanSize bytes")
}

/**
 * Throws for an element of [elementSize] bytes at [offset] that lies outside a span of [size] bytes. It is a function of its
 * own so that an accessor holds only the comparison.
 */
internal fun throwElementOutOfBounds(offset: Int, elementSize: Int, size: Int): Nothing =
    throw IndexOutOfBoundsException("Element of $elementSize bytes at offset $offset is outside a span of $size bytes")

/**
 * Checks a range of array indices with the rules of `copyInto`.
 */
@PublishedApi
internal fun checkArrayRange(startIndex: Int, endIndex: Int, arraySize: Int) {
    if (startIndex < 0 || endIndex > arraySize) {
        throw IndexOutOfBoundsException("Range $startIndex until $endIndex is outside an array of size $arraySize")
    }
    if (startIndex > endIndex) {
        throw IllegalArgumentException("Start index $startIndex is greater than end index $endIndex")
    }
}

/**
 * Checks a transfer between a span of [spanSize] bytes at [offset] and an array range, and returns its size in bytes.
 */
internal fun checkArrayTransfer(offset: Int, startIndex: Int, endIndex: Int, arraySize: Int, elementSize: Int, spanSize: Int): Int {
    checkArrayRange(startIndex, endIndex, arraySize)
    val count = endIndex - startIndex
    // A range whose bytes an Int cannot count is larger than any span.
    if (count > Int.MAX_VALUE / elementSize) {
        throwRangeOutOfBounds(offset.toLong(), count.toLong() * elementSize, spanSize.toLong())
    }
    val byteCount = count * elementSize
    checkSpanRange(offset, byteCount, spanSize)
    return byteCount
}

/**
 * Checks a range of array indices with the rules of `copyInto`, for a span over its elements of [elementSize] bytes, and
 * returns the size of that span in bytes.
 *
 * @throws IllegalArgumentException when the range holds more bytes than a span can.
 */
@PublishedApi
internal fun checkArraySpan(startIndex: Int, endIndex: Int, arraySize: Int, elementSize: Int): Int {
    checkArrayRange(startIndex, endIndex, arraySize)
    val count = endIndex - startIndex
    require(count <= Int.MAX_VALUE / elementSize) {
        "A range of $count elements of $elementSize bytes holds more bytes than a span can, which is ${Int.MAX_VALUE}"
    }
    return count * elementSize
}
