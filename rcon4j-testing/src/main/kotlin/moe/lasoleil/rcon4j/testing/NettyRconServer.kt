package moe.lasoleil.rcon4j.testing

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import moe.lasoleil.rcon4j.RconPacket
import moe.lasoleil.rcon4j.RconPacketDecoder
import moe.lasoleil.rcon4j.RconPacketEncoder
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

data class ClientSession(
    var authenticated: Boolean = false,
    var currentId: Int = 0
)

class EmbeddedRconServer(
    private val password: String,
    port: Int
) {
    @Volatile
    var port: Int = port
        private set
    val sessions = ConcurrentHashMap<Channel, ClientSession>()
    @Volatile
    var lastAuthId = 0
    @Volatile
    var lastAuthResponseId = 0
    @Volatile
    var lastCommand: String? = null
    @Volatile
    var shouldThrowException = false
    @Volatile
    var exceptionOccurred = false

    private val bossGroup = NioEventLoopGroup(1)
    private val workerGroup = NioEventLoopGroup()
    @Volatile
    private var channel: Channel? = null

    fun start() {
        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline()
                        .addLast(RconPacketDecoder())
                        .addLast(RconPacketEncoder())
                        .addLast(object : SimpleChannelInboundHandler<RconPacket>() {
                            override fun channelRead0(ctx: ChannelHandlerContext, msg: RconPacket) {
                                try {
                                    if (shouldThrowException && msg is RconPacket.ExecCommand) {
                                        throw RuntimeException("Simulated server error")
                                    }

                                    when (msg) {
                                        is RconPacket.Auth -> handleAuth(ctx, msg)
                                        is RconPacket.ExecCommand -> handleCommand(ctx, msg)
                                    }
                                } catch (t: Throwable) {
                                    exceptionOccurred = true
                                    handleException(ctx, msg, t)
                                }
                            }

                            private fun handleAuth(ctx: ChannelHandlerContext, auth: RconPacket.Auth) {
                                lastAuthId = auth.id()
                                val isAuth = auth.payload().toString(Charsets.UTF_8) == password
                                val response = RconPacket.AuthResponse(if (isAuth) auth.id() else -1)
                                if (isAuth) {
                                    sessions.computeIfAbsent(ctx.channel()) { ClientSession() }.authenticated = true
                                }
                                lastAuthResponseId = response.id()
                                ctx.writeAndFlush(response)
                            }

                            private fun handleCommand(ctx: ChannelHandlerContext, cmd: RconPacket.ExecCommand) {
                                val command = cmd.payload().toString(Charsets.UTF_8)
                                lastCommand = command
                                val session = sessions.computeIfAbsent(ctx.channel()) { ClientSession() }
                                if (session.authenticated) {
                                    val matcher = command.lowercase()
                                    val responseText = when {
                                        matcher.startsWith("echo ") -> command.substringAfter("echo ")
                                        matcher == "ping" -> "pong"
                                        matcher == "version" -> VERSION
                                        else -> "unknown command: $command"
                                    }
                                    ctx.writeAndFlush(RconPacket.ResponseValue(cmd.id(), responseText))
                                } else {
                                    ctx.writeAndFlush(RconPacket.ResponseValue(cmd.id(), "unauthorized".toByteArray()))
                                }
                            }

                            private fun handleException(ctx: ChannelHandlerContext, msg: RconPacket, t: Throwable) {
                                val errorMsg = "Internal server error: ${t.message}".toByteArray()
                                ctx.writeAndFlush(RconPacket.ResponseValue(msg.id(), errorMsg))
                                println("Error handling packet: ${msg.javaClass.simpleName}")
                                t.printStackTrace()
                            }

                            override fun channelInactive(ctx: ChannelHandlerContext) {
                                sessions.remove(ctx.channel())
                                super.channelInactive(ctx)
                            }
                        })
                }
            })

        val channel = bootstrap.bind(port).sync().channel()
        this.port = (channel.localAddress() as InetSocketAddress).port
        this.channel = channel
    }

    fun stop() {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
        channel?.close()
    }

    companion object {
        const val VERSION = "RCON Server 1.0"
    }
}