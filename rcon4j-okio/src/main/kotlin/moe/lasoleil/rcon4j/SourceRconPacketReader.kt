package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException
import okio.BufferedSource
import java.io.IOException

object SourceRconPacketReader : RconPacket.Reader<BufferedSource> {
    @Throws(IOException::class)
    override fun read(source: BufferedSource): RconPacket {
        val length = source.readIntLe()
        val id = source.readIntLe()
        val type = source.readIntLe()
        val payload = ByteArray(length - 4 - 4 - 2)
        val realLength = source.read(payload)
        if (realLength != payload.size) {
            throw MalformedPacketException("Excepted payload length=" + payload.size + ", real length=" + realLength)
        }
        if (source.readByte() != 0.toByte() || source.readByte() != 0.toByte()) {
            throw MalformedPacketException("Should be packet end")
        }
        return Util.createS2CPacket(id, type, payload)
    }

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    inline fun BufferedSource.readRconPacket() = read(this)
}
