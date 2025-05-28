package moe.lasoleil.rcon4j;

import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public interface RconClient extends Closeable {

    String host();

    int port();

    @NotNull
    RconPacket send(RconPacket packet) throws IOException;

    default boolean authenticate(@NotNull String password) throws IOException {
        return authenticate(password, Util.randomPacketId());
    }

    default boolean authenticate(@NotNull String password, int id) throws IOException {
        if (id == -1) {
            throw new IllegalArgumentException("id for authentication cannot be -1");
        }

        RconPacket result = send(new RconPacket.Auth(id, password));
        if (!(result instanceof RconPacket.AuthResponse)) {
            throw new IllegalStateException("unexpected result: " + result);
        }

        if (result.id() != -1 && result.id() != id) {
            throw new IllegalStateException("unexpected result id: " + result.id() + ", expected " + id + "(success) or -1(failure)");
        }

        return result.id() != -1;
    }

    @NotNull
    default String command(@NotNull String payload) throws IOException {
        return command(payload, Util.randomPacketId());
    }

    @NotNull
    default String command(@NotNull String payload, int id) throws IOException {
        RconPacket result = send(new RconPacket.ExecCommand(id, payload));

        if (!(result instanceof RconPacket.ResponseValue)) {
            throw new IllegalStateException("unexpected result: " + result);
        }

        if (result.id() != id) {
            throw new IllegalStateException("unexpected result id: " + result.id() + ", expected " + id);
        }

        return new String(result.payload(), StandardCharsets.UTF_8);
    }

}
