package com.ayush.imagesteganographylibrary.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SteganographyCoreTest {

    @Test
    fun `encoded message is read back`() {
        val pixels = noise(500, 500)

        val encoded = SteganographyCore.encode(pixels, 500, 500, MESSAGE, PASSWORD)

        assertEquals(MESSAGE, SteganographyCore.decode(encoded, 500, 500, PASSWORD))
    }

    @Test
    fun `carrier is left visually intact`() {
        val pixels = noise(500, 500)

        val encoded = SteganographyCore.encode(pixels, 500, 500, MESSAGE, PASSWORD)

        pixels.forEachIndexed { index, pixel ->
            for (shift in intArrayOf(16, 8, 0)) {
                val before = (pixel shr shift) and 0xFF
                val after = (encoded[index] shr shift) and 0xFF
                assertTrue("channel moved by more than 3 at pixel $index", Math.abs(before - after) <= 3)
            }
        }
    }

    @Test
    fun `image larger than one block round trips`() {
        val pixels = noise(600, 600)

        val encoded = SteganographyCore.encode(pixels, 600, 600, MESSAGE, PASSWORD)

        assertEquals(MESSAGE, SteganographyCore.decode(encoded, 600, 600, PASSWORD))
    }

    @Test
    fun `blocks the message does not reach are untouched`() {
        val pixels = noise(1024, 1024)

        val encoded = SteganographyCore.encode(pixels, 1024, 1024, MESSAGE, PASSWORD)

        // The message fits in the first 512x512 block, the other three are copied as they are.
        assertArrayEquals(
            pixels.copyOfRange(512, 1024),
            encoded.copyOfRange(512, 1024),
        )
    }

    @Test
    fun `wrong password is rejected`() {
        val encoded = SteganographyCore.encode(noise(500, 500), 500, 500, MESSAGE, PASSWORD)

        assertThrows(Exception::class.java) {
            SteganographyCore.decode(encoded, 500, 500, "hi@wrong.com")
        }
    }

    @Test
    fun `plain image carries no message`() {
        assertNull(SteganographyCore.decode(noise(500, 500), 500, 500, PASSWORD))
    }

    @Test
    fun `encoded pixels are opaque`() {
        val pixels = noise(500, 500)
        pixels[0] = 0x00FFFFFF

        val encoded = SteganographyCore.encode(pixels, 500, 500, MESSAGE, PASSWORD)

        assertEquals(0xFF, (encoded[0] ushr 24) and 0xFF)
    }

    @Test
    fun `too small carrier is reported`() {
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            PixelSteganography.encode(noise(8, 8), 8, 8, MESSAGE)
        }

        assertTrue(thrown.message!!.contains("message needs"))
    }

    @Test
    fun `encoding is reproducible`() {
        val pixels = noise(500, 500)

        val first = SteganographyCore.encode(pixels, 500, 500, MESSAGE, PASSWORD)
        val second = SteganographyCore.encode(pixels, 500, 500, MESSAGE, PASSWORD)

        assertArrayEquals(first, second)
    }

    @Test
    fun `different passwords produce different images`() {
        val pixels = noise(500, 500)

        val first = SteganographyCore.encode(pixels, 500, 500, MESSAGE, "hi@readmio.comX8")
        val second = SteganographyCore.encode(pixels, 500, 500, MESSAGE, "hi@readmio.comI7")

        assertNotEquals(0, first.zip(second.toTypedArray()).count { (a, b) -> a != b })
    }

    @Test
    fun `progress is reported for every message byte`() {
        val reported = mutableListOf<Int>()

        PixelSteganography.encode(noise(500, 500), 500, 500, MESSAGE) { reported += it }

        assertEquals(PixelSteganography.messageByteCount(MESSAGE), reported.size)
        assertEquals((1..reported.size).toList(), reported)
    }

    @Test
    fun `short password is padded and long one truncated`() {
        assertEquals("hi@readmio.com##", SecretKeys.normalize("hi@readmio.com"))
        assertEquals("hi@readmio.comX8", SecretKeys.normalize("hi@readmio.comX8"))
        assertEquals(15, SecretKeys.normalize("a password longer than sixteen").length)
    }

    private fun noise(width: Int, height: Int): IntArray {
        val random = Random(42)
        return IntArray(width * height) { 0xFF000000.toInt() or random.nextInt(0xFFFFFF) }
    }

    private companion object {
        const val PASSWORD = "hi@example.com"
        const val MESSAGE = "u3AHyMUNzOS6pVqn2ka4Chr4DjiPZFZu@MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A"
    }
}
