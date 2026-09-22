package com.ayush.imagesteganographylibrary.core

/**
 * Entry point of the platform independent steganography: encrypts a message with a password and
 * hides it in the two least significant bits of every colour channel.
 *
 * Android wraps this with Bitmap conversions, build tooling wraps it with BufferedImage ones.
 * Keeping the algorithm here means an image written by a build can always be read by the app.
 */
object SteganographyCore {

    /**
     * Encrypts and hides [message].
     *
     * @param pixels ARGB pixels of the carrier image, row major
     * @param password password of at most [SecretKeys.KEY_LENGTH] characters
     * @return a new pixel array carrying the message
     */
    @JvmStatic
    @Throws(Exception::class)
    fun encode(pixels: IntArray, width: Int, height: Int, message: String, password: String): IntArray {
        val encrypted = Crypto.encryptMessage(message, SecretKeys.normalize(password))
        return PixelSteganography.encode(pixels, width, height, encrypted)
    }

    /**
     * Reads back a message hidden by [encode].
     *
     * @return the decrypted message, or null when the image carries none
     * @throws Exception when the image carries a message but the password does not open it
     */
    @JvmStatic
    @Throws(Exception::class)
    fun decode(pixels: IntArray, width: Int, height: Int, password: String): String? {
        val encrypted = PixelSteganography.decode(pixels, width, height)
        if (encrypted.isEmpty()) {
            return null
        }
        return Crypto.decryptMessage(encrypted, SecretKeys.normalize(password))
    }
}
