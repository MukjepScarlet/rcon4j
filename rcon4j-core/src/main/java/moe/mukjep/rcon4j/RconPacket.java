package moe.mukjep.rcon4j;

import moe.mukjep.rcon4j.exceptions.MalformedPacketException;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class RconPacket {
    private final int id;
    private final int type;
    private final byte[] payload;

    public RconPacket(int requestId, int type, byte[] payload) {
        this.id = requestId;
        this.type = type;
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "RconPacket{" +
                "id=" + id +
                ", type=" + type +
                ", payload=" + Arrays.toString(payload) +
                '}';
    }

    public byte[] toBytes() {
        return createPacket(id, type, payload);
    }

    public static byte[] createPacket(int id, int type, byte[] payload) {
        int bodyLength = 4 + 4 + payload.length + 2;
        int packetLength = 4 + bodyLength;

        ByteBuffer buffer = ByteBuffer.allocate(packetLength).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(bodyLength);
        buffer.putInt(id);
        buffer.putInt(type);
        buffer.put(payload);
        buffer.put((byte) 0);
        buffer.put((byte) 0);

        return buffer.array();
    }

    public static RconPacket fromStream(InputStream stream) throws IOException {
        try {
            byte[] header = new byte[4 * 3];
            stream.read(header);

            ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

            int length = buffer.getInt();
            int id = buffer.getInt();
            int type = buffer.getInt();

            byte[] payload = new byte[length - 4 - 4 - 2];
            DataInputStream dis = new DataInputStream(stream);

            dis.readFully(payload);
            dis.readByte();
            dis.readByte();

            return new RconPacket(id, type, payload);
        } catch (BufferUnderflowException | EOFException e) {
            throw new MalformedPacketException("Packet read error");
        }
    }
}
