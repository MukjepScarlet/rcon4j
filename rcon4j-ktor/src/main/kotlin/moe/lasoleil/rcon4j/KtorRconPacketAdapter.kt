package moe.lasoleil.rcon4j

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.readByte
import io.ktor.utils.io.readByteArray
import io.ktor.utils.io.readInt
import io.ktor.utils.io.writeByte
import io.ktor.utils.io.writeByteArray
import io.ktor.utils.io.writeInt
import moe.lasoleil.rcon4j.exceptions.MalformedPacketException

suspend fun ByteWriteChannel.writeRconPacket(packet: RconPacket) {
    suspend fun ByteWriteChannel.writeIntLe(value: Int) = writeInt(Util.swapEndian(value))

    writeIntLe(packet.length())
    writeIntLe(packet.id())
    writeIntLe(packet.type())
    writeByteArray(packet.payload())
    writeByte(0.toByte())
    writeByte(0.toByte())
    flush()
}

suspend fun ByteReadChannel.readRconPacket(): RconPacket {
    suspend fun ByteReadChannel.readIntLe() = Util.swapEndian(readInt())

    val length = readIntLe()
    val id = readIntLe()
    val type = readIntLe()
    val payload = readByteArray(length - 4 - 4 - 2)
    if (readByte() != 0.toByte() || readByte() != 0.toByte()) {
        throw MalformedPacketException("Should be packet end")
    }
    return Util.createS2CPacket(id, type, payload)
}
