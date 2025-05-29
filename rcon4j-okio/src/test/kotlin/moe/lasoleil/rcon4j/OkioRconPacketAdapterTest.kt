package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8
import okio.EOFException
import kotlin.test.*

class OkioRconPacketAdapterTest {
    private val validAuthPacket = RconPacket.Auth(123, "password")
    private val validResponsePacket = RconPacket.ResponseValue(456, "result")

    @Test
    fun testWriteAndReadAuthPacket() {
        val buffer = Buffer()

        OkioRconPacketAdapter.write(buffer, validAuthPacket)

        val readPacket = OkioRconPacketAdapter.read(buffer)

        assertIs<RconPacket.Auth>(readPacket)
        assertEquals(validAuthPacket.id(), readPacket.id())
        assertEquals(validAuthPacket.type(), readPacket.type())
        assertContentEquals(validAuthPacket.payload(), readPacket.payload())
    }

    @Test
    fun testWriteAndReadResponsePacket() {
        val buffer = Buffer()

        OkioRconPacketAdapter.write(buffer, validResponsePacket)
        val readPacket = OkioRconPacketAdapter.read(buffer)

        assert(readPacket is RconPacket.ResponseValue)
        assertEquals(validResponsePacket.id(), readPacket.id())
        assertEquals(validResponsePacket.type(), readPacket.type())
        assertContentEquals(validResponsePacket.payload(), readPacket.payload())
    }

    @Test
    fun testUnexpectedEOFDuringRead() {
        val buffer = Buffer()

        buffer.writeIntLe(14).writeShort(0x1234)

        assertFailsWith<EOFException> {
            OkioRconPacketAdapter.read(buffer)
        }
    }

    @Test
    fun testInvalidTerminators() {
        val buffer = Buffer()

        OkioRconPacketAdapter.write(buffer, validAuthPacket)

        val byteArray = buffer.readByteArray().apply {
            this[this.size - 1] = 0
            this[this.size - 2] = 1
        }

        buffer.write(byteArray)

        assertFailsWith<MalformedPacketException> {
            OkioRconPacketAdapter.read(buffer)
        }
    }

    @Test
    fun testPayloadLengthMismatch() {
        val buffer = Buffer()

        val payload = "test".toByteArray()
        val expectedLength = 4 + 4 + payload.size + 2
        val fakeLength = expectedLength + 10

        buffer.writeIntLe(fakeLength)
            .writeIntLe(1)
            .writeIntLe(RconPacket.SERVERDATA_EXECCOMMAND)
            .write(payload)
            .writeByte(0)
            .writeByte(0)
        buffer.flush()

        assertFailsWith<EOFException> {
            OkioRconPacketAdapter.read(buffer)
        }
    }

    @Test
    fun testUnexpectedEOFAfterPayload() {
        val buffer = Buffer()

        buffer.writeIntLe(14) // length = 4+4+4+2=14
            .writeIntLe(1)
            .writeIntLe(RconPacket.SERVERDATA_AUTH)
            .write("pass".encodeUtf8()) // payload=4
        // stop earlier

        assertFailsWith<EOFException> {
            OkioRconPacketAdapter.read(buffer)
        }
    }

    @Test
    fun testZeroLengthPayload() {
        val packet = RconPacket.AuthResponse(789) // payload=empty
        val buffer = Buffer()

        OkioRconPacketAdapter.write(buffer, packet)
        val readPacket = OkioRconPacketAdapter.read(buffer)

        assert(readPacket is RconPacket.AuthResponse)
        assertEquals(packet.id(), readPacket.id())
        assertContentEquals(Util.EMPTY_BYTE_ARRAY, readPacket.payload())
    }

    @Test
    fun testLargePayload() {
        val largePayload = ByteArray(1024 * 10) { it.toByte() } // 10KB payload
        val packet = RconPacket.ExecCommand(999, largePayload)
        val buffer = Buffer()

        OkioRconPacketAdapter.write(buffer, packet)
        val readPacket = OkioRconPacketAdapter.read(buffer)

        assertIs<RconPacket.ExecCommand>(readPacket)
        assertContentEquals(largePayload, readPacket.payload())
    }
}
