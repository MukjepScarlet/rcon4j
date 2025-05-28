package moe.lasoleil.rcon4j;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

public final class RconPacketEncoder extends MessageToByteEncoder<RconPacket> {

    public static final RconPacketEncoder INSTANCE = new RconPacketEncoder();

    private RconPacketEncoder() {}

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RconPacket packet, ByteBuf byteBuf) throws IOException {
        ByteBufRconPacketAdapter.INSTANCE.write(byteBuf, packet);
    }
}
