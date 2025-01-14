package moe.mukjep.rcon4j

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import moe.mukjep.rcon4j.exceptions.AuthenticationException
import java.nio.charset.Charset
import kotlin.random.Random

suspend fun ARconClient(
    host: String,
    port: Int,
    password: String,
    charset: Charset = Charsets.UTF_8,
    requestId: Int = Random.nextInt(0, Int.MAX_VALUE) + 1,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ARconClient = ARconClient(
    aSocket(ActorSelectorManager(dispatcher)).tcp().connect(host, port),
    charset,
    requestId
).apply {
    authenticate(password)
}

class ARconClient internal constructor(
    private val socket: Socket,
    private val charset: Charset,
    private val requestId: Int
) : AutoCloseable {
    private val input: ByteReadChannel = socket.openReadChannel()
    private val output: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)

    internal suspend fun authenticate(password: String) {
        val response = send(PacketTypes.SERVERDATA_AUTH, password.toByteArray(charset))
        if (response.id == -1) {
            throw AuthenticationException("Authentication failed")
        }
    }

    override fun close() = socket.close()

    suspend fun command(payload: String): String {
        require(payload.isNotEmpty()) { "Payload can't be empty" }

        val response = send(PacketTypes.SERVERDATA_EXECCOMMAND, payload.toByteArray(charset))

        if (response.id != requestId) {
            throw IllegalStateException("Unexpected request id: " + response.id)
        }

        return response.payload.toString(charset)
    }

    private suspend fun send(type: Int, payload: ByteArray): RconPacket {
        output.writeFully(RconPacket.createPacket(requestId, type, payload))
        return input.readRconPacket()
    }
}
