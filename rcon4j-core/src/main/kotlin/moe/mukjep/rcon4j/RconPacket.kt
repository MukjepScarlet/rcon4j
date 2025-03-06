package moe.mukjep.rcon4j

import moe.mukjep.rcon4j.exceptions.MalformedPacketException
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RconPacket(val id: Int, val type: Int, val payload: ByteArray) {

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return "RconPacket{" +
                "id=" + id +
                ", type=" + type +
                ", payload=" + payload.toHexString() +
                '}'
    }

    fun toBytes(): ByteArray {
        return bytes(id, type, payload)
    }

    companion object {
        @JvmStatic
        fun bytes(id: Int, type: Int, payload: ByteArray): ByteArray {
            val bodyLength = 4 + 4 + payload.size + 2
            val packetLength = 4 + bodyLength

            val buffer = ByteBuffer.allocate(packetLength).order(ByteOrder.LITTLE_ENDIAN)

            buffer.putInt(bodyLength)
            buffer.putInt(id)
            buffer.putInt(type)
            buffer.put(payload)
            buffer.put(0.toByte())
            buffer.put(0.toByte())

            return buffer.array()
        }

        @JvmStatic
        @JvmName("from")
        @Throws(IOException::class)
        fun InputStream.readRconPacket(): RconPacket {
            try {
                val header = ByteArray(4 * 3)
                read(header)

                val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

                val length = buffer.getInt()
                val id = buffer.getInt()
                val type = buffer.getInt()

                val payload = ByteArray(length - 4 - 4 - 2)

                read(payload)
                read()
                read()

                return RconPacket(id, type, payload)
            } catch (e: BufferUnderflowException) {
                throw MalformedPacketException("Packet read error")
            } catch (e: EOFException) {
                throw MalformedPacketException("Packet read error")
            }
        }
    }
}
