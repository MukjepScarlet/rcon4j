package moe.lasoleil.rcon4j;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

public final class DefaultRconClient implements RconClient {

    private final String host;
    private final int port;
    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;

    public DefaultRconClient(@NotNull String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String host() {
        return this.host;
    }

    @Override
    public int port() {
        return this.port;
    }

    @Override
    public @NotNull RconPacket send(RconPacket packet) throws IOException {
        if (this.socket == null) {
            this.socket = new Socket(host, port);
            this.in = new BufferedInputStream(this.socket.getInputStream());
            this.out = new BufferedOutputStream(this.socket.getOutputStream());
        }

        RconPacket.Writer.OutputStream.write(this.out, packet);
        return RconPacket.Reader.InputStream.read(this.in);
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
