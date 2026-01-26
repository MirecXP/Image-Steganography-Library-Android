package com.ayush.imagesteganographylibrary

import com.ayush.imagesteganographylibrary.Utils.Utility
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilityTest {

    @Test
    fun `squareBlockNeeded returns 1 for pixels fitting in single block`() {
        // 512 * 512 = 262144 pixels per block
        assertEquals(1, Utility.squareBlockNeeded(1))
        assertEquals(1, Utility.squareBlockNeeded(100))
        assertEquals(1, Utility.squareBlockNeeded(262144))
    }

    @Test
    fun `squareBlockNeeded returns 2 when pixels exceed single block`() {
        assertEquals(2, Utility.squareBlockNeeded(262145))
        assertEquals(2, Utility.squareBlockNeeded(500000))
    }

    @Test
    fun `squareBlockNeeded handles large pixel counts`() {
        // 3 full blocks
        assertEquals(3, Utility.squareBlockNeeded(262144 * 3))
        // 3 full blocks + 1 pixel = 4 blocks needed
        assertEquals(4, Utility.squareBlockNeeded(262144 * 3 + 1))
    }

    @Test
    fun `isStringEmpty returns true for null`() {
        assertTrue(Utility.isStringEmpty(null))
    }

    @Test
    fun `isStringEmpty returns true for empty string`() {
        assertTrue(Utility.isStringEmpty(""))
    }

    @Test
    fun `isStringEmpty returns true for whitespace only`() {
        assertTrue(Utility.isStringEmpty("   "))
        assertTrue(Utility.isStringEmpty("\t\n"))
    }

    @Test
    fun `isStringEmpty returns true for undefined`() {
        assertTrue(Utility.isStringEmpty("undefined"))
    }

    @Test
    fun `isStringEmpty returns false for valid string`() {
        assertFalse(Utility.isStringEmpty("hello"))
        assertFalse(Utility.isStringEmpty("a"))
        assertFalse(Utility.isStringEmpty("  text  "))
    }

    @Test
    fun `convertArray converts int RGB to byte array`() {
        // Single pixel: RGB(255, 128, 64) = 0x00FF8040
        val input = intArrayOf(0x00FF8040)
        val result = Utility.convertArray(input)

        assertEquals(3, result.size)
        assertEquals(0xFF.toByte(), result[0]) // R
        assertEquals(0x80.toByte(), result[1]) // G
        assertEquals(0x40.toByte(), result[2]) // B
    }

    @Test
    fun `convertArray handles multiple pixels`() {
        // Two pixels
        val input = intArrayOf(0x00FF0000, 0x0000FF00) // Red, Green
        val result = Utility.convertArray(input)

        assertEquals(6, result.size)
        // First pixel (red)
        assertEquals(0xFF.toByte(), result[0])
        assertEquals(0x00.toByte(), result[1])
        assertEquals(0x00.toByte(), result[2])
        // Second pixel (green)
        assertEquals(0x00.toByte(), result[3])
        assertEquals(0xFF.toByte(), result[4])
        assertEquals(0x00.toByte(), result[5])
    }

    @Test
    fun `convertArray handles black and white`() {
        val input = intArrayOf(0x00000000, 0x00FFFFFF) // Black, White
        val result = Utility.convertArray(input)

        // Black
        assertEquals(0x00.toByte(), result[0])
        assertEquals(0x00.toByte(), result[1])
        assertEquals(0x00.toByte(), result[2])
        // White
        assertEquals(0xFF.toByte(), result[3])
        assertEquals(0xFF.toByte(), result[4])
        assertEquals(0xFF.toByte(), result[5])
    }

    @Test
    fun `byteArrayToInt converts 3 bytes to int`() {
        val bytes = byteArrayOf(0xFF.toByte(), 0x80.toByte(), 0x40.toByte())
        val result = Utility.byteArrayToInt(bytes)
        assertEquals(0x00FF8040, result)
    }

    // Note: byteArrayToIntArray tests are skipped because the method uses android.util.Log
    // which is not available in unit tests. These would need to be instrumented tests
    // or the Utility class would need to be refactored to remove Log calls.
}
