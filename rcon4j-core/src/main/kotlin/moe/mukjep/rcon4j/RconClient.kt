package moe.mukjep.rcon4j

import moe.mukjep.rcon4j.RconPacket.Companion.readRconPacket
import moe.mukjep.rcon4j.exceptions.AuthenticationException
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.Socket
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.random.Random

class RconClient @JvmOverloads @Throws(IOException::class, AuthenticationException::class) constructor(
    host: String,
    port: Int,
    password: String,
    private val charset: Charset = StandardCharsets.UTF_8,
    private val requestId: Int = Random.nextInt(1, Int.MAX_VALUE)
) : AutoCloseable {
    init {
        require(requestId >= 0) { "requestId can't be negative" }
    }

    private val socket = Socket(host, port)
    private val input = BufferedInputStream(socket.getInputStream())
    private val output = BufferedOutputStream(socket.getOutputStream())

    init {
        val response = send(PacketTypes.SERVERDATA_AUTH, password.toByteArray(charset))
        if (response.id == -1) {
            throw AuthenticationException("Authentication failed")
        }
    }

    @Throws(IOException::class)
    override fun close() {
        socket.close()
    }

    @Throws(IOException::class)
    fun command(payload: String): String {
        require(payload.isNotEmpty()) { "Payload can't be empty" }

        val response = send(PacketTypes.SERVERDATA_EXECCOMMAND, payload.toByteArray(charset))

        check(response.id == requestId) { "Unexpected request id: " + response.id }

        return response.payload.toString(charset)
    }

    @Throws(IOException::class)
    private fun send(type: Int, payload: ByteArray): RconPacket {
        output.write(RconPacket.bytes(requestId, type, payload))
        output.flush()
        return input.readRconPacket()
    }
}