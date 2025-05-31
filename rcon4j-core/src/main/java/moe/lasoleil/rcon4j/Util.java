package moe.lasoleil.rcon4j;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.ThreadLocalRandom;

final class Util {
    private Util() {}

    static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    static int randomPacketId() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    static byte @NotNull [] requireNotNullOrEmpty(byte @Nullable [] value) {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("value cannot be null or empty");
        }
        return value;
    }

    static int readInt32Le(@NotNull InputStream in) throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException("Unexpected end of stream");
        }
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) | ((b4 & 0xFF) << 24);
    }

    static void writeInt32Le(@NotNull OutputStream out, int value) throws IOException {
        out.write(value & 0xFF);          // byte 1 - lowest
        out.write((value >> 8) & 0xFF);   // byte 2
        out.write((value >> 16) & 0xFF);  // byte 3
        out.write((value >> 24) & 0xFF);  // byte 4 - highest
    }

    static int readInt32Le(byte @NotNull [] in, int startIndex) {
        byte b1 = in[startIndex++];
        byte b2 = in[startIndex++];
        byte b3 = in[startIndex++];
        byte b4 = in[startIndex];
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) | ((b4 & 0xFF) << 24);
    }

    static void writeInt32Le(byte @NotNull [] out, int value, int startIndex) {
        out[startIndex++] = (byte) (value & 0xFF);          // byte 1 - lowest
        out[startIndex++] = (byte) ((value >> 8) & 0xFF);   // byte 2
        out[startIndex++] = (byte) ((value >> 16) & 0xFF);  // byte 3
        out[startIndex] = (byte) ((value >> 24) & 0xFF);    // byte 4 - highest
    }

    static int swapEndian(int value) {
        return ((value >>> 24)) |
                ((value >> 8) & 0x0000FF00) |
                ((value << 8) & 0x00FF0000) |
                ((value << 24));
    }

    static @NotNull String packetToString(@NotNull String name, @NotNull RconPacket packet) {
        return name + "[length=" +
                packet.length() +
                ", id=" +
                packet.id() +
                ", type=" +
                packet.type() +
                ", payload=" +
                Base64.getEncoder().encodeToString(packet.payload()) +
                "]";
    }

    static int packetHashCode(@NotNull RconPacket packet) {
        int result = Integer.hashCode(packet.length());
        result = 31 * result + Integer.hashCode(packet.id());
        result = 31 * result + Integer.hashCode(packet.type());
        result = 31 * result + Arrays.hashCode(packet.payload());
        return result;
    }

    static boolean packetEquals(@NotNull RconPacket a, @NotNull RconPacket b) {
        return a.length() == b.length() && a.id() == b.id() && a.type() == b.type() && Arrays.equals(a.payload(), b.payload());
    }

    static @NotNull RconPacket createPacket(int id, int type, byte @NotNull [] payload) {
        switch (type) {
            case 0: // SERVERDATA_RESPONSE_VALUE
                return new RconPacket.ResponseValue(id, payload);
            case 2: // SERVERDATA_EXECCOMMAND, SERVERDATA_AUTH_RESPONSE
                return payload.length == 0 ? new RconPacket.AuthResponse(id) : new RconPacket.ExecCommand(id, payload);
            case 3: // SERVERDATA_AUTH
                return new RconPacket.Auth(id, payload);
            default:
                throw new IllegalArgumentException("Unknown packet type: " + type);
        }
    }

}
