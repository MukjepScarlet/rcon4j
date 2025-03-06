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
    requestId: Int = Random.nextInt(1, Int.MAX_VALUE),
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ARconClient = ARconClient(host, port, password, charset, requestId, ActorSelectorManager(dispatcher))

suspend fun ARconClient(
    host: String,
    port: Int,
    password: String,
    charset: Charset = Charsets.UTF_8,
    requestId: Int = Random.nextInt(1, Int.MAX_VALUE),
    actorSelectorManager: ActorSelectorManager,
): ARconClient = ARconClient(host, port, password, charset, requestId, aSocket(actorSelectorManager))

suspend fun ARconClient(
    host: String,
    port: Int,
    password: String,
    charset: Charset = Charsets.UTF_8,
    requestId: Int = Random.nextInt(1, Int.MAX_VALUE),
    socketBuilder: SocketBuilder,
): ARconClient {
    require(requestId >= 0) { "requestId can't be negative" }

    return ARconClient(
        socketBuilder.tcp().connect(host, port),
        charset,
        requestId
    ).apply {
        authenticate(password)
    }
}

class ARconClient internal constructor(
    private val socket: Socket,
    private val charset: Charset,
    private val requestId: Int
) : AutoCloseable {
    private val input: ByteReadChannel = socket.openReadChannel()
    private val output: ByteWriteChannel = socket.openWriteChannel()

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

        check(response.id == requestId) { "Unexpected request id: " + response.id }

        return response.payload.toString(charset)
    }

    private suspend fun send(type: Int, payload: ByteArray): RconPacket {
        output.writeFully(RconPacket.bytes(requestId, type, payload))
        output.flush()
        return input.readRconPacket()
    }
}
