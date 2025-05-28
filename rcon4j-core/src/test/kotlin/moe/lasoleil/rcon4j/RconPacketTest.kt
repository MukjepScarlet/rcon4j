package moe.lasoleil.rcon4j

import kotlin.test.*

class RconPacketTest {

    @Test
    fun testPacketLengthCalculation() {
        val authPacket = RconPacket.Auth(123, "password")
        assertEquals(18, authPacket.length()) // 4(id) + 4(type) + 8(payload) + 2(trailing)

        val responsePacket = RconPacket.ResponseValue(456, "Hello, World!")
        assertEquals(23, responsePacket.length()) // 4 + 4 + 13 + 2
    }

    @Test
    fun testAuthPacketCreation() {
        val auth = RconPacket.Auth(100, "test")
        assertEquals(100, auth.id())
        assertEquals(RconPacket.SERVERDATA_AUTH, auth.type())
        assertContentEquals("test".toByteArray(Charsets.UTF_8), auth.payload())
    }

    @Test
    fun testAuthResponsePacketCreation() {
        val authResponse = RconPacket.AuthResponse(200)
        assertEquals(200, authResponse.id())
        assertEquals(RconPacket.SERVERDATA_AUTH_RESPONSE, authResponse.type())
        assertContentEquals(Util.EMPTY_BYTE_ARRAY, authResponse.payload())
    }

    @Test
    fun testExecCommandPacketCreation() {
        val execCommand = RconPacket.ExecCommand(300, "status")
        assertEquals(300, execCommand.id())
        assertEquals(RconPacket.SERVERDATA_EXECCOMMAND, execCommand.type())
        assertContentEquals("status".toByteArray(Charsets.UTF_8), execCommand.payload())
    }

    @Test
    fun testResponseValuePacketCreation() {
        val responseValue = RconPacket.ResponseValue(400, "OK")
        assertEquals(400, responseValue.id())
        assertEquals(RconPacket.SERVERDATA_RESPONSE_VALUE, responseValue.type())
        assertContentEquals("OK".toByteArray(Charsets.UTF_8), responseValue.payload())
    }

    @Test
    fun testPacketEquality() {
        val packetA = RconPacket.Auth(500, "pass")
        val packetB = RconPacket.Auth(500, "pass")
        val packetC = RconPacket.Auth(501, "pass")

        assertTrue(packetA == packetB)
        assertFalse(packetA == packetC)
        assertFalse(packetA == null)
        assertFalse(packetA == Any())
    }

    @Test
    fun testPacketHashCode() {
        val packetA = RconPacket.ExecCommand(600, "cmd")
        val packetB = RconPacket.ExecCommand(600, "cmd")

        assertEquals(packetA.hashCode(), packetB.hashCode())
    }

    @Test
    fun testPacketToString() {
        val packet = RconPacket.ResponseValue(700, "data") // int32, int32, byte *4, 0x00, 0x00 -> 14
        val expected = "RconPacket.ResponseValue[length=14, id=700, type=0, payload=" +
                "ZGF0YQ==" + // "data" in Base64
                "]"
        assertEquals(expected, packet.toString())
    }

    @Test
    fun testToByteArray() {
        val packet = RconPacket.Auth(800, "secret")
        val byteArray = RconPacket.toByteArray(packet)

        // Verify length field (first 4 bytes, little-endian)
        val length = Util.readInt32Le(byteArray, 0)
        assertEquals(packet.length(), length)

        // Verify ID field
        val id = Util.readInt32Le(byteArray, 4)
        assertEquals(800, id)

        // Verify Type field
        val type = Util.readInt32Le(byteArray, 8)
        assertEquals(RconPacket.SERVERDATA_AUTH, type)

        // Verify Payload
        val payload = byteArray.copyOfRange(12, 12 + "secret".length)
        assertContentEquals("secret".toByteArray(Charsets.UTF_8), payload)

        // Verify terminating null bytes
        assertEquals(0, byteArray[byteArray.size - 2])
        assertEquals(0, byteArray[byteArray.size - 1])
    }

    @Test
    fun testUtilCreateS2CPacket() {
        val authResponse = Util.createS2CPacket(900, RconPacket.SERVERDATA_AUTH_RESPONSE, Util.EMPTY_BYTE_ARRAY)
        assertTrue(authResponse is RconPacket.AuthResponse)
        assertEquals(900, authResponse.id())

        val responseValue = Util.createS2CPacket(901, RconPacket.SERVERDATA_RESPONSE_VALUE, "result".toByteArray())
        assertTrue(responseValue is RconPacket.ResponseValue)
        assertEquals(901, responseValue.id())
        assertContentEquals("result".toByteArray(), responseValue.payload())

        assertFailsWith<IllegalArgumentException> {
            Util.createS2CPacket(902, 999, Util.EMPTY_BYTE_ARRAY) // Invalid type
        }
    }
}
