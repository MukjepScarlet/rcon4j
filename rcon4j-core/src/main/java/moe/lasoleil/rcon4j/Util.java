package moe.lasoleil.rcon4j;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ThreadLocalRandom;

final class Util {
    private Util() {}

    static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    static int randomPacketId() {
        return ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
    }

    static int readInt32Le(InputStream in) throws IOException {
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        int b4 = in.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException("Unexpected end of stream");
        }
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) | ((b4 & 0xFF) << 24);
    }

    static void writeInt32Le(OutputStream out, int value) throws IOException {
        out.write(value & 0xFF);          // byte 1 - lowest
        out.write((value >> 8) & 0xFF);   // byte 2
        out.write((value >> 16) & 0xFF);  // byte 3
        out.write((value >> 24) & 0xFF);  // byte 4 - highest
    }

    static int readInt32Le(byte[] in, int startIndex) {
        byte b1 = in[startIndex++];
        byte b2 = in[startIndex++];
        byte b3 = in[startIndex++];
        byte b4 = in[startIndex];
        return (b1 & 0xFF) | ((b2 & 0xFF) << 8) | ((b3 & 0xFF) << 16) | ((b4 & 0xFF) << 24);
    }

    static void writeInt32Le(byte[] out, int value, int startIndex) {
        out[startIndex++] = (byte) (value & 0xFF);          // byte 1 - lowest
        out[startIndex++] = (byte) ((value >> 8) & 0xFF);   // byte 2
        out[startIndex++] = (byte) ((value >> 16) & 0xFF);  // byte 3
        out[startIndex] = (byte) ((value >> 24) & 0xFF);    // byte 4 - highest
    }

    static RconPacket createS2CPacket(int id, int type, byte[] payload) {
        switch (type) {
            case RconPacket.SERVERDATA_AUTH_RESPONSE:
                return new RconPacket.AuthResponse(id);
            case RconPacket.SERVERDATA_RESPONSE_VALUE:
                return new RconPacket.ResponseValue(id, payload);
            default:
                throw new IllegalArgumentException("Unknown packet type: " + type);
        }
    }

}
