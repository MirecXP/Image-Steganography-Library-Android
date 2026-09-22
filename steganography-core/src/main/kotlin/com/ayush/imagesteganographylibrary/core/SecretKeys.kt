package com.ayush.imagesteganographylibrary.core

/**
 * Brings a human readable password to the 128 bit length AES needs.
 */
object SecretKeys {

    /** AES-128 key length in characters - the key is used as raw bytes. */
    const val KEY_LENGTH = 16

    private const val PADDING = '#'

    /**
     * Pads a short password with `#` up to [KEY_LENGTH].
     *
     * Passwords longer than [KEY_LENGTH] are truncated to 15 characters, which is not a valid AES
     * key length - the original library behaves the same way and this method keeps that behaviour
     * so that images encoded by either implementation stay interchangeable. Keep the password at
     * [KEY_LENGTH] characters or shorter.
     */
    @JvmStatic
    fun normalize(secretKey: String): String =
        if (secretKey.length <= KEY_LENGTH) {
            secretKey.padEnd(KEY_LENGTH, PADDING)
        } else {
            secretKey.substring(0, KEY_LENGTH - 1)
        }
}
