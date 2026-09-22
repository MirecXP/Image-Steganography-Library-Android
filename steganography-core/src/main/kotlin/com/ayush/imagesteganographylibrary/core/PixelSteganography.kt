package com.ayush.imagesteganographylibrary.core

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.function.IntConsumer

/**
 * The 2-bit LSB codec, working on plain ARGB pixel arrays so that the same implementation serves
 * Android (Bitmap) and the JVM (BufferedImage).
 *
 * Two bits of every colour channel carry the message, so one message byte occupies four channels.
 * The message is framed with [START_MESSAGE_CONSTANT] and [END_MESSAGE_CONSTANT] and read back as
 * ISO-8859-1.
 *
 * The image is processed in 512x512 blocks in the same order the original library split the bitmap
 * in, and the channel counter restarts at every block. Both are load bearing: change them and
 * images encoded by an older version stop decoding.
 */
object PixelSteganography {

    /** Edge of the square block the image is processed in. */
    const val SQUARE_BLOCK_SIZE = 512

    private const val START_MESSAGE_CONSTANT = "@!#"
    private const val END_MESSAGE_CONSTANT = "#!@"
    private val MESSAGE_CHARSET: Charset = Charset.forName("ISO-8859-1")

    /** Bit offsets of the red, green and blue channel inside an ARGB pixel. */
    private val CHANNEL_SHIFT = intArrayOf(16, 8, 0)

    /** Where the 2 bits of the message byte sit, most significant pair first. */
    private val MESSAGE_SHIFT = intArrayOf(6, 4, 2, 0)

    private const val CHANNELS = 3
    private val CHANNELS_PER_MESSAGE_BYTE = MESSAGE_SHIFT.size
    private const val OPAQUE = 0xFF000000.toInt()

    /**
     * Number of pixels the given message needs, framing included.
     */
    @JvmStatic
    fun requiredPixels(message: String?): Int {
        if (message == null) {
            return -1
        }
        return messageByteCount(message) * CHANNELS_PER_MESSAGE_BYTE / CHANNELS
    }

    /**
     * Number of bytes written into the image for the given message, framing included.
     */
    @JvmStatic
    fun messageByteCount(message: String): Int = frame(message).toByteArray(MESSAGE_CHARSET).size

    private fun frame(message: String): String =
        START_MESSAGE_CONSTANT + message + END_MESSAGE_CONSTANT

    /**
     * Hides [message] in a copy of [pixels].
     *
     * @param pixels ARGB pixels of the whole image, row major
     * @param onByteEncoded called with the number of message bytes written so far, may be null
     * @return a new array with the message encoded; blocks the message does not reach are copied
     * unchanged, blocks that carry it become fully opaque
     * @throws IllegalArgumentException when the image is too small for the message
     */
    @JvmStatic
    @JvmOverloads
    fun encode(
        pixels: IntArray,
        width: Int,
        height: Int,
        message: String,
        onByteEncoded: IntConsumer? = null,
    ): IntArray {
        require(pixels.size == width * height) {
            "pixels length ${pixels.size} does not match ${width}x$height"
        }
        val required = requiredPixels(message)
        require(required <= pixels.size) {
            "image holds ${pixels.size} pixels, message needs $required"
        }

        val messageBytes = frame(message).toByteArray(MESSAGE_CHARSET)
        val result = pixels.copyOf()
        var messageIndex = 0

        for (block in blocks(width, height)) {
            if (messageIndex == messageBytes.size) {
                break
            }
            // The channel counter restarts at every block, mirroring the original implementation.
            var shiftIndex = CHANNELS_PER_MESSAGE_BYTE

            for (row in 0 until block.height) {
                for (col in 0 until block.width) {
                    val index = (block.y + row) * width + block.x + col
                    val pixel = pixels[index]
                    var encoded = OPAQUE

                    for (channel in 0 until CHANNELS) {
                        var value = (pixel shr CHANNEL_SHIFT[channel]) and 0xFF
                        if (messageIndex < messageBytes.size) {
                            val pair = (messageBytes[messageIndex].toInt() shr
                                MESSAGE_SHIFT[shiftIndex % CHANNELS_PER_MESSAGE_BYTE]) and 0x3
                            value = (value and 0xFC) or pair
                            shiftIndex++
                            if (shiftIndex % CHANNELS_PER_MESSAGE_BYTE == 0) {
                                messageIndex++
                                onByteEncoded?.accept(messageIndex)
                            }
                        }
                        encoded = encoded or (value shl CHANNEL_SHIFT[channel])
                    }
                    result[index] = encoded
                }
            }
        }
        return result
    }

    /**
     * Reads back a message hidden by [encode].
     *
     * @param pixels ARGB pixels of the whole image, row major
     * @return the framed message without its framing, or an empty string when the image carries none
     */
    @JvmStatic
    fun decode(pixels: IntArray, width: Int, height: Int): String {
        val message = StringBuilder()
        val raw = ByteArrayOutputStream()

        for (block in blocks(width, height)) {
            // The channel counter restarts at every block, mirroring the original implementation.
            var shiftIndex = CHANNELS_PER_MESSAGE_BYTE
            var current = 0

            for (row in 0 until block.height) {
                for (col in 0 until block.width) {
                    val pixel = pixels[(block.y + row) * width + block.x + col]

                    for (channel in 0 until CHANNELS) {
                        val pair = (pixel shr CHANNEL_SHIFT[channel]) and 0x3
                        current = current or (pair shl MESSAGE_SHIFT[shiftIndex % CHANNELS_PER_MESSAGE_BYTE])
                        shiftIndex++
                        if (shiftIndex % CHANNELS_PER_MESSAGE_BYTE != 0) {
                            continue
                        }
                        raw.write(current)

                        // The end marker is spotted one byte late, that byte is dropped below.
                        if (message.endsWith(END_MESSAGE_CONSTANT)) {
                            val bytes = raw.toByteArray()
                            return unframe(String(bytes, 0, bytes.size - 1, MESSAGE_CHARSET))
                        }
                        message.append((current and 0xFF).toChar())
                        if (message.length == START_MESSAGE_CONSTANT.length &&
                            !message.contentEquals(START_MESSAGE_CONSTANT)
                        ) {
                            // Nothing was ever encoded here.
                            return ""
                        }
                        current = 0
                    }
                }
            }
        }
        return unframe(message.toString())
    }

    private fun unframe(message: String): String = when {
        message.isBlank() -> ""
        message.length < START_MESSAGE_CONSTANT.length + END_MESSAGE_CONSTANT.length -> message
        else -> message.substring(
            START_MESSAGE_CONSTANT.length,
            message.length - END_MESSAGE_CONSTANT.length,
        )
    }

    /**
     * The 512x512 blocks of an image, in the order the original library split the bitmap in.
     */
    private fun blocks(width: Int, height: Int): List<Block> {
        val cols = divideRoundingUp(width)
        val rows = divideRoundingUp(height)
        val widthRemainder = width % SQUARE_BLOCK_SIZE
        val heightRemainder = height % SQUARE_BLOCK_SIZE

        return buildList(rows * cols) {
            for (row in 0 until rows) {
                for (col in 0 until cols) {
                    add(
                        Block(
                            x = col * SQUARE_BLOCK_SIZE,
                            y = row * SQUARE_BLOCK_SIZE,
                            width = if (col == cols - 1 && widthRemainder > 0) widthRemainder else SQUARE_BLOCK_SIZE,
                            height = if (row == rows - 1 && heightRemainder > 0) heightRemainder else SQUARE_BLOCK_SIZE,
                        )
                    )
                }
            }
        }
    }

    private fun divideRoundingUp(size: Int): Int =
        size / SQUARE_BLOCK_SIZE + if (size % SQUARE_BLOCK_SIZE > 0) 1 else 0

    private class Block(val x: Int, val y: Int, val width: Int, val height: Int)
}
