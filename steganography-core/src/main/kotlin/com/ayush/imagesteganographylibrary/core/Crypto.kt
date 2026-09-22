package com.ayush.imagesteganographylibrary.core

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * AES layer applied to the message before it is hidden in the pixels.
 *
 * The Base64 flavour mirrors `android.util.Base64` with flag `0` (DEFAULT): 76 character lines
 * separated by a single `\n` plus a trailing newline. [Base64]'s MIME encoder defaults to CRLF,
 * hence the explicit line separator.
 */
object Crypto {

    private const val BASE64_LINE_LENGTH = 76
    private val BASE64_LINE_SEPARATOR = byteArrayOf('\n'.code.toByte())

    /**
     * @param message plain text to protect
     * @param secretKey key of a length AES accepts, see [SecretKeys.normalize]
     * @return Base64 encoded cipher text
     */
    @JvmStatic
    @Throws(Exception::class)
    fun encryptMessage(message: String, secretKey: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(secretKey.toByteArray(), "AES"))
        val encrypted = cipher.doFinal(message.toByteArray())
        return Base64.getMimeEncoder(BASE64_LINE_LENGTH, BASE64_LINE_SEPARATOR)
            .encodeToString(encrypted) + "\n"
    }

    /**
     * @param encryptedMessage Base64 encoded cipher text, line breaks are tolerated
     * @param secretKey the key the message was encrypted with
     * @return the plain text
     * @throws Exception when the key is wrong - the padding check fails
     */
    @JvmStatic
    @Throws(Exception::class)
    fun decryptMessage(encryptedMessage: String, secretKey: String): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(secretKey.toByteArray(), "AES"))
        val decoded = Base64.getMimeDecoder().decode(encryptedMessage.toByteArray())
        return String(cipher.doFinal(decoded))
    }
}
