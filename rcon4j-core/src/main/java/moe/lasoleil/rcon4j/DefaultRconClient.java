package moe.lasoleil.rcon4j;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

final class DefaultRconClient implements RconClient {

    private final Socket socket = new Socket();
    private InputStream in = null;
    private OutputStream out = null;

    @Override
    public void connect(@NotNull InetAddress address, int port) throws IOException {
        socket.connect(new InetSocketAddress(address, port));
        this.in = new BufferedInputStream(this.socket.getInputStream());
        this.out = new BufferedOutputStream(this.socket.getOutputStream());
    }

    @Override
    public @NotNull RconPacket send(RconPacket packet) throws IOException {
        RconPacket.Writer.OutputStream.write(this.out, packet);
        return RconPacket.Reader.InputStream.read(this.in);
    }

    @Override
    public void close() throws IOException {
        this.socket.close();
    }
}
