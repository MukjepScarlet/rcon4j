package moe.lasoleil.rcon4j

import io.ktor.network.sockets.*
import io.ktor.utils.io.core.Closeable

fun Socket.rcon(): KtorRconClient = KtorRconClient(this)

class KtorRconClient internal constructor(socket: Socket) : Closeable by socket {

    private val readChannel = socket.openReadChannel()
    private val writeChannel = socket.openWriteChannel()

    suspend fun send(packet: RconPacket): RconPacket {
        writeChannel.writeRconPacket(packet)
        return readChannel.readRconPacket()
    }

    suspend fun authenticate(password: String, id: Int = Util.randomPacketId()): Boolean {
        require(id != -1) { "id for authentication cannot be -1" }

        val result = send(RconPacket.Auth(id, password))
        if (result !is RconPacket.AuthResponse) {
            error("unexpected result: $result")
        }

        if (result.id() != -1 && result.id() != id) {
            error("unexpected result id: ${result.id()}, expected $id(success) or -1(failure)")
        }

        return result.id() != -1
    }

    suspend fun command(payload: String, id: Int = Util.randomPacketId()):String {
        val result = send(RconPacket.ExecCommand(id, payload))

        if (result !is RconPacket.ResponseValue) {
            error("unexcepted result: $result")
        }

        if (result.id() != id) {
            error("unexpected result id: ${result.id()}, expected $id")
        }

        return result.payload().toString(Charsets.UTF_8)
    }

}
