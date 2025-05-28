package moe.lasoleil.rcon4j

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UtilTest {

    @Test
    fun testEmptyByteArray() {
        assertContentEquals(byteArrayOf(), Util.EMPTY_BYTE_ARRAY)
    }

    @Test
    fun testRandomPacketId() {
        val id = Util.randomPacketId()
        assertTrue(id in 1 until Integer.MAX_VALUE)
    }

    @Test
    fun testReadInt32Le_InputStream() {
        val testValue = 0x12345678
        val buffer = ByteArray(4)
        Util.writeInt32Le(buffer, testValue, 0)

        val input = ByteArrayInputStream(buffer)
        val result = Util.readInt32Le(input)
        assertEquals(testValue, result)
    }

    @Test
    fun testReadInt32Le_InputStream_EOF() {
        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3)) // Incomplete data
        assertFailsWith<EOFException> {
            Util.readInt32Le(input)
        }
    }

    @Test
    fun testWriteInt32Le_OutputStream() {
        val testValue = 0x12345678
        val output = ByteArrayOutputStream()

        Util.writeInt32Le(output, testValue)
        val result = output.toByteArray()

        val expected = ByteArray(4)
        Util.writeInt32Le(expected, testValue, 0)

        assertContentEquals(expected, result)
    }

    @Test
    fun testReadInt32Le_ByteArray() {
        val testValue = 0x12345678
        val buffer = ByteArray(4)
        Util.writeInt32Le(buffer, testValue, 0)

        val result = Util.readInt32Le(buffer, 0)
        assertEquals(testValue, result)
    }

    @Test
    fun testWriteInt32Le_ByteArray() {
        val testValue = 0x12345678
        val buffer = ByteArray(4)

        Util.writeInt32Le(buffer, testValue, 0)

        val b1 = buffer[0].toInt() and 0xFF
        val b2 = buffer[1].toInt() and 0xFF
        val b3 = buffer[2].toInt() and 0xFF
        val b4 = buffer[3].toInt() and 0xFF

        assertEquals(0x78, b1)
        assertEquals(0x56, b2)
        assertEquals(0x34, b3)
        assertEquals(0x12, b4)
    }

    @Test
    fun testSwapEndian() {
        val testValue = 0x12345678
        val swapped = Util.swapEndian(testValue)
        assertEquals(0x78563412, swapped)
    }

    @Test
    fun testSwapEndian_NegativeValue() {
        val testValue = -1 // 0xFFFFFFFF
        val swapped = Util.swapEndian(testValue)
        assertEquals(-1, swapped)
    }

    @Test
    fun testSwapEndian_Zero() {
        val testValue = 0
        val swapped = Util.swapEndian(testValue)
        assertEquals(0, swapped)
    }

    @Test
    fun testSwapEndian_SingleByte() {
        val testValue = 0x000000FF
        val swapped = Util.swapEndian(testValue)
        assertEquals(0xFF000000.toInt(), swapped)
    }
}
