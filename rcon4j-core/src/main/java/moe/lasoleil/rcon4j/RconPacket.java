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

    default byte @NotNull [] payload() {
        return Util.EMPTY_BYTE_ARRAY;
    }

    static byte @NotNull [] toByteArray(RconPacket packet) {
        byte[] result = new byte[packet.length() + 4];
        RconPacketAdapters.forByteArray().write(result, packet);
        return result;
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

        @Override
        public int hashCode() {
            return Util.packetHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Auth)) return false;
            return Util.packetEquals(this, (Auth) obj);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.Auth", this);
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

        @Override
        public int hashCode() {
            return Util.packetHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof AuthResponse)) return false;
            return Util.packetEquals(this, (AuthResponse) obj);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.AuthResponse", this);
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
            if (payload.length == 0) {
                throw new IllegalArgumentException("payload cannot be empty");
            }
            this.id = id;
            this.payload = payload;
        }

        @Override
        public int hashCode() {
            return Util.packetHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ExecCommand)) return false;
            return Util.packetEquals(this, (ExecCommand) obj);
        }

        @Override
        public String toString() {
            return Util.packetToString("RconPacket.ExecCommand", this);
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

        @Override
        public int hashCode() {
            return Util.packetHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ResponseValue)) return false;
            return Util.packetEquals(this, (ResponseValue) obj);
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
