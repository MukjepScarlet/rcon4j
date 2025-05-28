package moe.lasoleil.rcon4j;

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException;

import java.io.InputStream;
import java.io.OutputStream;

public final class RconPacketAdapters {

    private RconPacketAdapters() {}

    private static final RconPacket.Reader<InputStream> fromInputStream = (in) -> {
        int length = Util.readInt32Le(in);
        int id = Util.readInt32Le(in);
        int type = Util.readInt32Le(in);
        byte[] payload = new byte[length - 4 - 4 - 2];
        int realLength = in.read(payload);
        if (realLength != payload.length) {
            throw new MalformedPacketException("Excepted payload length=" + payload.length + ", real length=" + realLength);
        }
        if (in.read() != 0 || in.read() != 0) {
            throw new MalformedPacketException("Should be packet end");
        }
        return Util.createS2CPacket(id, type, payload);
    };

    public static RconPacket.Reader<InputStream> fromInputStream() {
        return fromInputStream;
    }

    private static final RconPacket.Reader<byte[]> fromByteArray = (in) -> {
        if (in.length < 4 + 4 + 4 + 2) {
            throw new MalformedPacketException("Not enough bytes");
        }
        int length = Util.readInt32Le(in, 0);
        int id = Util.readInt32Le(in, 4);
        int type = Util.readInt32Le(in, 8);
        byte[] payload = new byte[length - 4 - 4 - 2];
        if (in.length - 4 < length) {
            throw new MalformedPacketException("Not enough bytes");
        }
        System.arraycopy(in, 4 + 4 + 4, payload, 0, payload.length);
        if (in[length + 4 - 2] != 0 || in[length + 4 - 1] != 0) {
            throw new MalformedPacketException("Should be packet end");
        }
        return Util.createS2CPacket(id, type, payload);
    };

    public static RconPacket.Reader<byte[]> fromByteArray() {
        return fromByteArray;
    }

    private static final RconPacket.Writer<OutputStream> toOutPutStream = (out, packet) -> {
        Util.writeInt32Le(out, packet.length());
        Util.writeInt32Le(out, packet.id());
        Util.writeInt32Le(out, packet.type());
        out.write(packet.payload());
        out.write(0);
        out.write(0);
    };

    public static RconPacket.Writer<OutputStream> toOutPutStream() {
        return toOutPutStream;
    }

    private static final RconPacket.Writer<byte[]> toByteArray = (out, packet) -> {
        if (out.length < 4 + packet.length()) {
            throw new IllegalArgumentException("Array too short");
        }
        Util.writeInt32Le(out, packet.length(), 0);
        Util.writeInt32Le(out, packet.id(), 4);
        Util.writeInt32Le(out, packet.type(), 8);
        System.arraycopy(packet.payload(), 0, out, 12, packet.payload().length);
        out[packet.length() - 2] = out[packet.length() - 1] = 0;
    };

    public static RconPacket.Writer<byte[]> toByteArray() {
        return toByteArray;
    }

}
