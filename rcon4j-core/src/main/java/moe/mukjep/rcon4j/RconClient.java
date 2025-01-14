package moe.mukjep.rcon4j;

import moe.mukjep.rcon4j.exceptions.AuthenticationException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public final class RconClient implements AutoCloseable {
    private final int requestId;

    private final Charset charset;

    private final Socket socket;

    public RconClient(String host, int port, String password) throws IOException, AuthenticationException {
        this(host, port, password, StandardCharsets.UTF_8);
    }

    public RconClient(String host, int port, String password, Charset charset) throws IOException, AuthenticationException {
        this(host, port, password, charset, new Random().nextInt(Integer.MAX_VALUE) + 1);
    }

    public RconClient(String host, int port, String password, Charset charset, int requestId) throws IOException, AuthenticationException {
        if (requestId < 0) {
            throw new IllegalArgumentException("requestId can't be negative");
        }

        this.requestId = requestId;
        this.charset = charset;
        this.socket = new Socket(host, port);
        RconPacket response = send(PacketTypes.SERVERDATA_AUTH, password.getBytes(charset));

        if (response.getId() == -1) {
            throw new AuthenticationException("Authentication failed");
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public String command(String payload) throws IOException {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("Payload can't be null or empty");
        }

        RconPacket response = send(PacketTypes.SERVERDATA_EXECCOMMAND, payload.getBytes(this.charset));

        if (response.getId() != this.requestId) {
            throw new IllegalStateException("Unexpected request id: " + response.getId());
        }

        return new String(response.getPayload(), this.charset);
    }

    private RconPacket send(int type, byte[] payload) throws IOException {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(RconPacket.createPacket(requestId, type, payload));
        outputStream.flush();
        return RconPacket.fromStream(socket.getInputStream());
    }

}
