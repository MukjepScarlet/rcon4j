package moe.lasoleil.rcon4j;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.IOException;
import java.util.List;

public final class RconPacketDecoder extends ByteToMessageDecoder {

    public static final RconPacketDecoder INSTANCE = new RconPacketDecoder();

    private RconPacketDecoder() {}

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws IOException {
        RconPacket packet = ByteBufRconPacketAdapter.INSTANCE.read(byteBuf);
        list.add(packet);
    }

}
