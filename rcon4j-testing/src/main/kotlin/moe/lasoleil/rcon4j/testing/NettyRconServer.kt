package moe.lasoleil.rcon4j.testing

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import moe.lasoleil.rcon4j.RconPacket
import moe.lasoleil.rcon4j.RconPacketDecoder
import moe.lasoleil.rcon4j.RconPacketEncoder

fun startNettyRconServer(
    password: String,
    port: Int,
) {
    val bossGroup = NioEventLoopGroup(1)
    val workerGroup = NioEventLoopGroup()
    try {
        val bootstrap = ServerBootstrap()
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel::class.java)
            .childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(socketChannel: SocketChannel) {
                    socketChannel.pipeline()
                        .addLast(RconPacketDecoder.INSTANCE)
                        .addLast(RconPacketEncoder.INSTANCE)
                        .addLast(object : SimpleChannelInboundHandler<RconPacket>() {
                            override fun channelRead0(
                                ctx: ChannelHandlerContext,
                                msg: RconPacket
                            ) {
                                val response = when (msg) {
                                    is RconPacket.Auth -> {
                                        val pw = msg.payload().toString(Charsets.UTF_8)
                                        if (pw == password) {
                                            RconPacket.AuthResponse(msg.id())
                                        } else {
                                            RconPacket.AuthResponse(-1)
                                        }
                                    }
                                    is RconPacket.ExecCommand -> {
                                        val command = msg.payload().toString(Charsets.UTF_8)
                                        val response = when (command) {
                                            "echo" -> command
                                            "ping" -> "pong"
                                            else -> "Unknown command: $command"
                                        }
                                        RconPacket.ResponseValue(msg.id(), response)
                                    }
                                    else -> error("Unsupported command: ${msg.javaClass.simpleName}")
                                }
                                ctx.writeAndFlush(response)
                            }
                        })
                }
            })
        bootstrap.bind(port).sync().channel().closeFuture().sync()
    } finally {
        bossGroup.shutdownGracefully()
        workerGroup.shutdownGracefully()
    }
}

