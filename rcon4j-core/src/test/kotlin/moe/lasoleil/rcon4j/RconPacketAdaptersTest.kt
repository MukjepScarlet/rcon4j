package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException
import kotlin.test.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel

class RconPacketAdaptersTest {

    @Test
    fun testForByteArray_Read() {
        val packet = RconPacket.ResponseValue(123, "test")
        val byteArray = ByteArray(4 + packet.length())
        RconPacketAdapters.forByteArray().write(byteArray, packet)

        val readPacket = RconPacketAdapters.forByteArray().read(byteArray)
        assertEquals(packet.id(), readPacket.id())
        assertEquals(packet.type(), readPacket.type())
        assertContentEquals(packet.payload(), readPacket.payload())
    }

    @Test
    fun testForByteArray_Read_InvalidLength() {
        val invalidData = ByteArray(10)
        assertFailsWith<MalformedPacketException> {
            RconPacketAdapters.forByteArray().read(invalidData)
        }
    }

    @Test
    fun testForByteArray_Read_MissingTerminators() {
        val packet = RconPacket.Auth(456, "pass")
        val byteArray = ByteArray(4 + packet.length())
        RconPacketAdapters.forByteArray().write(byteArray, packet)
        byteArray[byteArray.size - 1] = 1 // 破坏终止符

        assertFailsWith<MalformedPacketException> {
            RconPacketAdapters.forByteArray().read(byteArray)
        }
    }

    @Test
    fun testForByteArray_Write_ArrayTooShort() {
        val packet = RconPacket.ExecCommand(789, "cmd")
        val byteArray = ByteArray(10) // 故意创建过小的数组

        assertFailsWith<IllegalArgumentException> {
            RconPacketAdapters.forByteArray().write(byteArray, packet)
        }
    }

    @Test
    fun testForInputStream_C2S_Read() {
        val packet = RconPacket.AuthResponse(111)
        val out = ByteArrayOutputStream()
        RconPacketAdapters.forOutPutStream().write(out, packet)
        val input = ByteArrayInputStream(out.toByteArray())

        val readPacket = RconPacketAdapters.forInputStream().read(input)
        assertEquals(packet.id(), readPacket.id())
        assertEquals(packet.type(), readPacket.type())
    }

    @Test
    fun testForInputStream_C2S_Read_EOF() {
        val input = ByteArrayInputStream(byteArrayOf(1, 2, 3, 4))
        assertFailsWith<EOFException> {
            RconPacketAdapters.forInputStream().read(input)
        }
    }

    @Test
    fun testForOutputStream_Write() {
        val packet = RconPacket.ResponseValue(222, "data")
        val out = ByteArrayOutputStream()
        RconPacketAdapters.forOutPutStream().write(out, packet)

        val byteArray = out.toByteArray()
        val readPacket = RconPacketAdapters.forByteArray().read(byteArray)
        assertEquals(packet.id(), readPacket.id())
        assertEquals(packet.type(), readPacket.type())
        assertContentEquals(packet.payload(), readPacket.payload())
    }

    @Test
    fun testForByteBuffer_Read() {
        val packet = RconPacket.ResponseValue(333, "status")
        val buffer = ByteBuffer.allocate(4 + packet.length())
        RconPacketAdapters.forByteBuffer().write(buffer, packet)
        buffer.flip()

        val readPacket = RconPacketAdapters.forByteBuffer().read(buffer)
        assertEquals(packet.id(), readPacket.id())
        assertEquals(packet.type(), readPacket.type())
        assertContentEquals(packet.payload(), readPacket.payload())
    }

    @Test
    fun testForByteBuffer_Read_Underflow() {
        val buffer = ByteBuffer.allocate(10)
        assertFailsWith<MalformedPacketException> {
            RconPacketAdapters.forByteBuffer().read(buffer)
        }
    }

    @Test
    fun testForByteBuffer_Write_InsufficientCapacity() {
        val packet = RconPacket.Auth(444, "pass")
        val buffer = ByteBuffer.allocate(10)

        assertFailsWith<IllegalArgumentException> {
            RconPacketAdapters.forByteBuffer().write(buffer, packet)
        }
    }

    @Test
    fun testForReadableByteChannel_Read() {
        val packet = RconPacket.ResponseValue(555, "result")
        val outBuffer = ByteBuffer.allocate(4 + packet.length())
        RconPacketAdapters.forByteBuffer().write(outBuffer, packet)
        outBuffer.flip()

        val channel = Channels.newChannel(ByteArrayInputStream(outBuffer.array()))
        val readPacket = RconPacketAdapters.forReadableByteChannel().read(channel)

        assertEquals(packet.id(), readPacket.id())
        assertEquals(packet.type(), readPacket.type())
        assertContentEquals(packet.payload(), readPacket.payload())
    }

    @Test
    fun testForReadableByteChannel_Read_ChannelClosedEarly() {
        val channel = object : ReadableByteChannel {
            private var closed = false
            override fun read(dst: ByteBuffer): Int = throw IOException("Simulated channel closure")
            override fun isOpen() = !closed
            override fun close() { closed = true }
        }

        assertFailsWith<IOException> {
            RconPacketAdapters.forReadableByteChannel().read(channel)
        }
    }

    @Test
    fun testForWritableByteChannel_Write() {
        val packet = RconPacket.ResponseValue(666, "secure")
        val outBuffer = ByteArrayOutputStream()
        val channel = Channels.newChannel(outBuffer)

        RconPacketAdapters.forWritableByteChannel().write(channel, packet)
        val byteArray = outBuffer.toByteArray()

        val readPacket = RconPacketAdapters.forByteArray().read(byteArray)
        assertEquals(packet.id(), readPacket.id())
        assertEquals(packet.type(), readPacket.type())
        assertContentEquals(packet.payload(), readPacket.payload())
    }

    @Test
    fun testForWritableByteChannel_Write_Failure() {
        val packet = RconPacket.ExecCommand(777, "test")
        val channel = object : WritableByteChannel {
            private var closed = false
            override fun write(src: ByteBuffer): Int = throw IOException("Simulated write failure")
            override fun isOpen() = !closed
            override fun close() { closed = true }
        }

        assertFailsWith<IOException> {
            RconPacketAdapters.forWritableByteChannel().write(channel, packet)
        }
    }
}
