package moe.lasoleil.rcon4j;

import io.netty.buffer.ByteBuf;
import moe.lasoleil.rcon4j.exceptions.MalformedPacketException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class ByteBufRconPacketAdapter implements RconPacket.Writer<ByteBuf>, RconPacket.Reader<ByteBuf> {

    public static final ByteBufRconPacketAdapter INSTANCE = new ByteBufRconPacketAdapter();

    private ByteBufRconPacketAdapter() {}

    @NotNull
    @Override
    public RconPacket read(@NotNull ByteBuf in) throws IOException {
        int length = in.readIntLE();
        int id = in.readIntLE();
        int type = in.readIntLE();
        byte[] payload = new byte[length - 4 - 4 - 2];
        if (in.readableBytes() < payload.length) {
            throw new MalformedPacketException("Excepted payload length=" + payload.length + ", real length=" + in.readableBytes());
        }
        in.readBytes(payload);
        if (in.readByte() != 0 || in.readByte() != 0) {
            throw new MalformedPacketException("Invalid packet terminators");
        }
        return Util.createPacket(id, type, payload);
    }

    @Override
    public void write(@NotNull ByteBuf out, @NotNull RconPacket packet) {
        out.writeIntLE(packet.length())
                .writeIntLE(packet.id())
                .writeIntLE(packet.type())
                .writeBytes(packet.payload())
                .writeByte(0)
                .writeByte(0);
    }
}
