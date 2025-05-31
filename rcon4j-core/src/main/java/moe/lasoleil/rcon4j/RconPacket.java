package moe.lasoleil.rcon4j;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface RconPacket {

    default int length() {
        return 4 + 4 + payload().length + 2;
    }

    int id();

    int type();

    byte @NotNull [] payload();

    static byte @NotNull [] toByteArray(RconPacket packet) {
        byte[] result = new byte[packet.length() + 4];
        RconPacketAdapters.forByteArray().write(result, packet);
        return result;
    }

    abstract class Base implements RconPacket {
        private final int id;
        private final int type;
        private final byte @NotNull [] payload;

        protected Base(int id, int type, byte @NotNull [] payload) {
            this.id = id;
            this.type = type;
            this.payload = payload;
        }

        @Override
        public int id() {
            return this.id;
        }

        @Override
        public int type() {
            return this.type;
        }

        @Override
        public byte @NotNull [] payload() {
            return this.payload;
        }

        @Override
        public final int hashCode() {
            return Util.packetHashCode(this);
        }

        @Override
        public final boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RconPacket)) return false;
            return Util.packetEquals(this, (RconPacket) obj);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.Base", this);
        }
    }

    final class Auth extends Base {
        public Auth(int id, @NotNull String password) {
            this(id, password.getBytes(StandardCharsets.UTF_8));
        }

        public Auth(int id, byte @NotNull [] payload) {
            super(id, SERVERDATA_AUTH, payload);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.Auth", this);
        }
    }

    final class AuthResponse extends Base {
        public AuthResponse(int id) {
            super(id, SERVERDATA_AUTH_RESPONSE, Util.EMPTY_BYTE_ARRAY);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.AuthResponse", this);
        }
    }

    final class ExecCommand extends Base {
        public ExecCommand(int id, @NotNull String command) {
            this(id, command.getBytes(StandardCharsets.UTF_8));
        }

        public ExecCommand(int id, byte @NotNull [] payload) {
            super(id, SERVERDATA_EXECCOMMAND, Util.requireNotNullOrEmpty(payload));
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.ExecCommand", this);
        }
    }

    final class ResponseValue extends Base {
        public ResponseValue(int id, @NotNull String content) {
            this(id, content.getBytes(StandardCharsets.UTF_8));
        }

        public ResponseValue(int id, byte @NotNull [] payload) {
            super(id, SERVERDATA_RESPONSE_VALUE, payload);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.ResponseValue", this);
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
    }

    @FunctionalInterface
    interface Writer<T> {
        void write(@NotNull T out, @NotNull RconPacket packet) throws IOException;
    }

}
