package moe.lasoleil.rcon4j;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

final class DefaultRconClient implements RconClient {

    private final Socket socket = new Socket();
    private InputStream in = null;
    private OutputStream out = null;

    @Override
    public void connect(@NotNull InetAddress address, int port) throws IOException {
        this.socket.connect(new InetSocketAddress(address, port));
        this.in = new BufferedInputStream(this.socket.getInputStream());
        this.out = new BufferedOutputStream(this.socket.getOutputStream());
    }

    @Override
    public @NotNull RconPacket send(@NotNull RconPacket packet) throws IOException {
        InputStream inputStream = this.in;
        OutputStream outputStream = this.out;
        if (inputStream == null || outputStream == null) {
            throw new IllegalStateException("Socket not connected");
        }

        RconPacket.Writer.OutputStream.write(outputStream, packet);
        return RconPacket.Reader.InputStream.read(inputStream);
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
