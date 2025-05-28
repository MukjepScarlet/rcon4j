package moe.lasoleil.rcon4j

import okio.BufferedSink
import java.io.IOException

object SinkRconPacketWriter : RconPacket.Writer<BufferedSink> {
    @Throws(IOException::class)
    override fun write(out: BufferedSink, packet: RconPacket) {
        out.writeIntLe(packet.length())
            .writeIntLe(packet.id())
            .writeIntLe(packet.type())
            .write(packet.payload())
            .writeByte(0)
            .writeByte(0)
    }

    @Suppress("NOTHING_TO_INLINE")
    @JvmSynthetic
    inline fun BufferedSink.writeRconPacket(packet: RconPacket) = write(this, packet)
}
