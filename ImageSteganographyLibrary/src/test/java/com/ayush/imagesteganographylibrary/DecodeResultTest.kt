package com.ayush.imagesteganographylibrary

import com.ayush.imagesteganographylibrary.Text.DecodeResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DecodeResultTest {

    @Test
    fun `Success contains message`() {
        val result = DecodeResult.Success("secret message")
        assertEquals("secret message", result.message)
    }

    @Test
    fun `Success with same message are equal`() {
        val result1 = DecodeResult.Success("test")
        val result2 = DecodeResult.Success("test")
        assertEquals(result1, result2)
    }

    @Test
    fun `Success with different messages are not equal`() {
        val result1 = DecodeResult.Success("test1")
        val result2 = DecodeResult.Success("test2")
        assertNotEquals(result1, result2)
    }

    @Test
    fun `NoMessageFound is singleton`() {
        val result1 = DecodeResult.NoMessageFound
        val result2 = DecodeResult.NoMessageFound
        assertTrue(result1 === result2)
    }

    @Test
    fun `WrongSecretKey is singleton`() {
        val result1 = DecodeResult.WrongSecretKey
        val result2 = DecodeResult.WrongSecretKey
        assertTrue(result1 === result2)
    }

    @Test
    fun `Error contains throwable`() {
        val exception = IllegalArgumentException("test error")
        val result = DecodeResult.Error(exception)
        assertEquals(exception, result.cause)
        assertEquals("test error", result.cause.message)
    }

    @Test
    fun `when expression covers all cases`() {
        val results = listOf(
            DecodeResult.Success("msg"),
            DecodeResult.NoMessageFound,
            DecodeResult.WrongSecretKey,
            DecodeResult.Error(RuntimeException())
        )

        results.forEach { result ->
            val description = when (result) {
                is DecodeResult.Success -> "success: ${result.message}"
                is DecodeResult.NoMessageFound -> "no message"
                is DecodeResult.WrongSecretKey -> "wrong key"
                is DecodeResult.Error -> "error: ${result.cause}"
            }
            assertTrue(description.isNotEmpty())
        }
    }

    @Test
    fun `different result types are not equal`() {
        val success = DecodeResult.Success("")
        val noMessage = DecodeResult.NoMessageFound
        val wrongKey = DecodeResult.WrongSecretKey
        val error = DecodeResult.Error(RuntimeException())

        assertNotEquals(success, noMessage)
        assertNotEquals(success, wrongKey)
        assertNotEquals(success, error)
        assertNotEquals(noMessage, wrongKey)
        assertNotEquals(noMessage, error)
        assertNotEquals(wrongKey, error)
    }
}
