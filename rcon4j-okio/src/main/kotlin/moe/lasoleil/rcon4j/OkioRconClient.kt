package moe.lasoleil.rcon4j

import moe.lasoleil.rcon4j.OkioRconPacketAdapter.readRconPacket
import moe.lasoleil.rcon4j.OkioRconPacketAdapter.writeRconPacket
import okio.BufferedSink
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class OkioRconClient : RconClient {

    private val socket = Socket()
    private var source: BufferedSource? = null
    private var sink: BufferedSink? = null

    @Throws(IOException::class)
    override fun connect(address: InetAddress, port: Int) {
        this.socket.connect(InetSocketAddress(address, port))
        this.source = this.socket.source().buffer()
        this.sink = this.socket.sink().buffer()
    }

    @Throws(IOException::class)
    override fun send(packet: RconPacket): RconPacket {
        val source = this.source
        val sink = this.sink
        if (source == null || sink == null) {
            throw IllegalStateException("Socket not connected")
        }

        sink.writeRconPacket(packet)
        return source.readRconPacket()
    }

    @Throws(IOException::class)
    override fun close() = socket.close()

}