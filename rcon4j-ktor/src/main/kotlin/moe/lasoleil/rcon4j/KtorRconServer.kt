package moe.lasoleil.rcon4j

import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

typealias OnRconConnect = suspend (client: Socket) -> Unit
typealias OnRconPacket = suspend (client: Socket, packet: RconPacket) -> RconPacket
typealias OnRconDisconnect = suspend (client: Socket) -> Unit
typealias OnRconError = suspend (client: Socket, error: Throwable) -> Unit

fun ServerSocket.rcon(): KtorRconServer.Builder = KtorRconServer.Builder(this)

class KtorRconServer private constructor(
    serverSocket: ServerSocket,
    val onConnect: OnRconConnect,
    val onPacket: OnRconPacket,
    val onDisconnect: OnRconDisconnect,
    val onError: OnRconError,
) : Closeable by serverSocket {

    private val serverSocketScope = CoroutineScope( SupervisorJob(serverSocket.socketContext))

    init {
        serverSocketScope.launch {
            while (isActive) {
                val socket = serverSocket.accept()

                launch {
                    try {
                        onConnect(socket)

                        val readChannel = socket.openReadChannel()
                        val writeChannel = socket.openWriteChannel()

                        while (socket.isActive) {
                            try {
                                val packet = readChannel.readRconPacket()
                                val response = onPacket(socket, packet)
                                writeChannel.writeRconPacket(response)
                            } catch (e: Throwable) {
                                onError(socket, e)
                            }
                        }

                        onDisconnect(socket)
                    } catch (_: CancellationException) {
                    } catch (e: Throwable) {
                        onError(socket, e)
                    }
                }
            }
        }
    }

    class Builder internal constructor(private val serverSocket: ServerSocket) {
        private var onConnect: OnRconConnect = {}
        private var onPacket: OnRconPacket? = null
        private var onDisconnect: OnRconDisconnect = {}
        private var onError: OnRconError = { _, _ -> }

        fun onConnect(onConnect: OnRconConnect) = apply {
            this.onConnect = onConnect
        }

        fun onPacket(onPacket: OnRconPacket) = apply {
            this.onPacket = onPacket
        }

        fun onDisconnect(onDisconnect: OnRconDisconnect) = apply {
            this.onDisconnect = onDisconnect
        }

        fun onError(onError: OnRconError) = apply {
            this.onError = onError
        }

        fun build(): KtorRconServer = KtorRconServer(
            serverSocket,
            onConnect = onConnect,
            onPacket = requireNotNull(onPacket) { "Handler for RconPacket is required" },
            onDisconnect = onDisconnect,
            onError = onError,
        )

    }

}
