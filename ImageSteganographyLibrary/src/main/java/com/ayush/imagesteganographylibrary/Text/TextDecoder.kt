package com.ayush.imagesteganographylibrary.Text

import android.graphics.Bitmap
import com.ayush.imagesteganographylibrary.core.Crypto
import com.ayush.imagesteganographylibrary.core.PixelSteganography
import com.ayush.imagesteganographylibrary.core.SecretKeys
import com.ayush.imagesteganographylibrary.core.SteganographyCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Coroutine-based text decoder for extracting hidden messages from images.
 * This is the modern replacement for the deprecated [TextDecoding] AsyncTask.
 *
 * The decoding itself lives in [SteganographyCore], which is also what build tooling encodes
 * with - one implementation on both sides.
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
     * @param secretKey The secret key used to decrypt the message, at most 16 characters
     * @return A [DecodeResult] indicating the outcome of the decoding operation
     */
    suspend fun decode(image: Bitmap, secretKey: String): DecodeResult =
        withContext(Dispatchers.Default) {
            val width = image.width
            val height = image.height
            val pixels = IntArray(width * height)
            image.getPixels(pixels, 0, width, 0, 0, width, height)

            val hidden = try {
                PixelSteganography.decode(pixels, width, height)
            } catch (e: Exception) {
                return@withContext DecodeResult.Error(e)
            }
            if (hidden.isEmpty()) {
                return@withContext DecodeResult.NoMessageFound
            }

            try {
                val decrypted = Crypto.decryptMessage(hidden, SecretKeys.normalize(secretKey))
                if (decrypted.isEmpty()) {
                    DecodeResult.WrongSecretKey
                } else {
                    DecodeResult.Success(decrypted)
                }
            } catch (e: Exception) {
                // A wrong key fails the padding check rather than returning garbage.
                DecodeResult.WrongSecretKey
            }
        }
}
