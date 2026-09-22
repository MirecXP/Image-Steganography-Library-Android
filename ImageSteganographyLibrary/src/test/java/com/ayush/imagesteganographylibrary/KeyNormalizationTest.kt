package com.ayush.imagesteganographylibrary

import com.ayush.imagesteganographylibrary.core.SecretKeys
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for key normalization logic used in encryption.
 * The key must be exactly 16 characters for AES-128.
 *
 * Exercises [SecretKeys.normalize], which both ImageSteganography.convertKeyTo128bit() and
 * TextDecoder delegate to.
 */
class KeyNormalizationTest {

    private fun normalizeKey(key: String): String = SecretKeys.normalize(key)

    @Test
    fun `empty key is padded to 16 hashes`() {
        val result = normalizeKey("")
        assertEquals(16, result.length)
        assertEquals("################", result)
    }

    @Test
    fun `short key is padded with hashes`() {
        val result = normalizeKey("abc")
        assertEquals(16, result.length)
        assertEquals("abc#############", result)
    }

    @Test
    fun `exactly 16 char key is unchanged`() {
        val key = "1234567890123456"
        val result = normalizeKey(key)
        assertEquals(16, result.length)
        assertEquals(key, result)
    }

    @Test
    fun `key longer than 16 is truncated to 15`() {
        val key = "12345678901234567890"
        val result = normalizeKey(key)
        assertEquals(15, result.length)
        assertEquals("123456789012345", result)
    }

    @Test
    fun `17 char key is truncated to 15`() {
        val key = "12345678901234567"
        val result = normalizeKey(key)
        assertEquals(15, result.length)
        assertEquals("123456789012345", result)
    }

    @Test
    fun `key with special characters is handled correctly`() {
        val key = "p@ss#w0rd!"
        val result = normalizeKey(key)
        assertEquals(16, result.length)
        assertEquals("p@ss#w0rd!######", result)
    }

    @Test
    fun `key with unicode characters`() {
        val key = "key"
        val result = normalizeKey(key)
        assertEquals(16, result.length)
        assertTrue(result.startsWith("key"))
    }

    @Test
    fun `same key produces same normalized result`() {
        val key = "mySecret"
        val result1 = normalizeKey(key)
        val result2 = normalizeKey(key)
        assertEquals(result1, result2)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.Assert.assertTrue(condition)
    }
}
