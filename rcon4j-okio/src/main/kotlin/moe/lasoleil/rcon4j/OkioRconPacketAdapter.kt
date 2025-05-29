package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException
import okio.BufferedSink
import okio.BufferedSource
import java.io.IOException

object OkioRconPacketAdapter : RconPacket.Writer<BufferedSink>, RconPacket.Reader<BufferedSource> {
    @Throws(IOException::class)
    override fun write(out: BufferedSink, packet: RconPacket) {
        out.writeIntLe(packet.length())
            .writeIntLe(packet.id())
            .writeIntLe(packet.type())
            .write(packet.payload())
            .writeByte(0)
            .writeByte(0)
        out.flush()
    }

    @Throws(IOException::class)
    override fun read(source: BufferedSource): RconPacket {
        val length = source.readIntLe()
        val id = source.readIntLe()
        val type = source.readIntLe()
        val payload = source.readByteArray((length - 4 - 4 - 2).toLong())
        if (source.readByte() != 0.toByte() || source.readByte() != 0.toByte()) {
            throw MalformedPacketException("Invalid packet terminators")
        }
        return Util.createPacket(id, type, payload)
    }

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    inline fun BufferedSink.writeRconPacket(packet: RconPacket) = write(this, packet)

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    inline fun BufferedSource.readRconPacket() = read(this)

}
