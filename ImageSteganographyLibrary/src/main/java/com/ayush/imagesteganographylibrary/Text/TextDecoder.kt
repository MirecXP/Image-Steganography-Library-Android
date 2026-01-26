package com.ayush.imagesteganographylibrary.Text

import android.graphics.Bitmap
import com.ayush.imagesteganographylibrary.Utils.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coroutine-based text decoder for extracting hidden messages from images.
 * This is the modern replacement for the deprecated [TextDecoding] AsyncTask.
 *
 * Usage example:
 * ```kotlin
 * lifecycleScope.launch {
 *     when (val result = TextDecoder.decode(bitmap, "mySecretKey")) {
 *         is DecodeResult.Success -> handleMessage(result.message)
 *         is DecodeResult.WrongSecretKey -> showError("Invalid key")
 *         is DecodeResult.NoMessageFound -> showError("No hidden message")
 *         is DecodeResult.Error -> showError(result.cause.message)
 *     }
 * }
 * ```
 */
object TextDecoder {

    /**
     * Decodes a hidden message from an image using the provided secret key.
     *
     * @param image The bitmap image that may contain a hidden message
     * @param secretKey The secret key used to decrypt the message
     * @return A [DecodeResult] indicating the outcome of the decoding operation
     */
    suspend fun decode(image: Bitmap, secretKey: String): DecodeResult =
        withContext(Dispatchers.Default) {
            try {
                val normalizedKey = normalizeKey(secretKey)
                val chunks = Utility.splitImage(image)

                val encodedMessage = EncodeDecode.decodeMessage(chunks)
                if (encodedMessage.isNullOrEmpty()) {
                    return@withContext DecodeResult.NoMessageFound
                }

                val decrypted = ImageSteganography.decryptMessage(encodedMessage, normalizedKey)
                if (decrypted.isNullOrEmpty()) {
                    return@withContext DecodeResult.WrongSecretKey
                }

                // Cleanup bitmap chunks to free memory
                chunks.forEach { it.recycle() }

                DecodeResult.Success(decrypted)
            } catch (e: Exception) {
                DecodeResult.Error(e)
            }
        }

    /**
     * Normalizes the secret key to 128-bit (16 characters) for AES encryption.
     * This matches the behavior of [ImageSteganography.convertKeyTo128bit].
     *
     * @param key The original secret key
     * @return A 16-character key (padded with '#' or truncated)
     */
    private fun normalizeKey(key: String): String {
        return if (key.length <= 16) {
            key.padEnd(16, '#')
        } else {
            key.substring(0, 15)
        }
    }
}
