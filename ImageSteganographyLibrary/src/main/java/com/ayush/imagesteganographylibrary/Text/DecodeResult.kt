package com.ayush.imagesteganographylibrary.Text

/**
 * Sealed class representing the result of a text decoding operation.
 * Provides type-safe handling of all possible outcomes.
 */
sealed class DecodeResult {
    /**
     * Decoding was successful and a message was extracted.
     * @param message The decoded and decrypted message
     */
    data class Success(val message: String) : DecodeResult()

    /**
     * No hidden message was found in the image.
     */
    data object NoMessageFound : DecodeResult()

    /**
     * The provided secret key was incorrect.
     */
    data object WrongSecretKey : DecodeResult()

    /**
     * An error occurred during decoding.
     * @param cause The exception that caused the error
     */
    data class Error(val cause: Throwable) : DecodeResult()
}
