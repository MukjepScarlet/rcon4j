package moe.lasoleil.rcon4j;

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public interface RconPacket {

    default int length() {
        return 4 + 4 + payload().length + 2;
    }

    int id();

    int type();

    default byte @NotNull [] payload() {
        return Util.EMPTY_BYTE_ARRAY;
    }

    final class Auth implements RconPacket {
        private final int id;
        private final byte[] payload;

        public int type() {
            return SERVERDATA_AUTH;
        }

        public int id() {
            return id;
        }

        public byte @NotNull [] payload() {
            return payload;
        }

        public Auth(int id, @NotNull String password) {
            this(id, password.getBytes(StandardCharsets.UTF_8));
        }

        public Auth(int id, byte @NotNull [] payload) {
            this.id = id;
            this.payload = payload;
        }
    }

    final class AuthResponse implements RconPacket {
        private final int id;

        public int type() {
            return SERVERDATA_AUTH_RESPONSE;
        }

        public int id() {
            return id;
        }

        public AuthResponse(int id) {
            this.id = id;
        }
    }

    final class ExecCommand implements RconPacket {
        private final int id;
        private final byte[] payload;

        public int type() {
            return SERVERDATA_EXECCOMMAND;
        }

        public int id() {
            return id;
        }

        public byte @NotNull [] payload() {
            return payload;
        }

        public ExecCommand(int id, @NotNull String command) {
            this(id, command.getBytes(StandardCharsets.UTF_8));
        }

        public ExecCommand(int id, byte @NotNull [] payload) {
            this.id = id;
            this.payload = payload;
        }
    }

    final class ResponseValue implements RconPacket {
        private final int id;
        private final byte[] payload;

        public int type() {
            return SERVERDATA_RESPONSE_VALUE;
        }

        public int id() {
            return id;
        }

        public byte @NotNull [] payload() {
            return payload;
        }

        public ResponseValue(int id, @NotNull String content) {
            this(id, content.getBytes(StandardCharsets.UTF_8));
        }

        public ResponseValue(int id, byte @NotNull [] payload) {
            this.id = id;
            this.payload = payload;
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    int SERVERDATA_RESPONSE_VALUE = 0;

    @SuppressWarnings("SpellCheckingInspection")
    int SERVERDATA_EXECCOMMAND = 2;

    @SuppressWarnings("SpellCheckingInspection")
    int SERVERDATA_AUTH_RESPONSE = 2;

    @SuppressWarnings("SpellCheckingInspection")
    int SERVERDATA_AUTH = 3;

    @FunctionalInterface
    interface Reader<T> {
        @NotNull RconPacket read(@NotNull T in) throws IOException;

        Reader<InputStream> InputStream = (in) -> {
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

        Reader<byte[]> ByteArray = (in) -> {
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
    }

    @FunctionalInterface
    interface Writer<T> {
        void write(@NotNull T out, @NotNull RconPacket packet) throws IOException;

        Writer<OutputStream> OutputStream = (out, packet) -> {
            Util.writeInt32Le(out, packet.length());
            Util.writeInt32Le(out, packet.id());
            Util.writeInt32Le(out, packet.type());
            out.write(packet.payload());
            out.write(0);
            out.write(0);
        };

        Writer<byte[]> ByteArray = (out, packet) -> {
            if (out.length < 4 + packet.length()) {
                throw new IllegalArgumentException("Array too short");
            }
            Util.writeInt32Le(out, packet.length(), 0);
            Util.writeInt32Le(out, packet.id(), 4);
            Util.writeInt32Le(out, packet.type(), 8);
            System.arraycopy(packet.payload(), 0, out, 12, packet.payload().length);
            out[packet.length() - 2] = out[packet.length() - 1] = 0;
        };
    }

}
