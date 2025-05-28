package moe.lasoleil.rcon4j;

import moe.lasoleil.rcon4j.exceptions.MalformedPacketException;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public final class RconPacketAdapters {

    private RconPacketAdapters() {}

    public static class ForByteArray implements RconPacket.Writer<byte[]>, RconPacket.Reader<byte[]> {

        private static final ForByteArray INSTANCE = new ForByteArray();

        private ForByteArray() {}

        @Override
        public @NotNull RconPacket read(byte @NotNull [] in) throws MalformedPacketException {
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
                throw new MalformedPacketException("Invalid packet terminators");
            }
            return Util.createS2CPacket(id, type, payload);
        }

        @Override
        public void write(byte @NotNull [] out, @NotNull RconPacket packet) throws IllegalArgumentException {
            if (out.length < 4 + packet.length()) {
                throw new IllegalArgumentException("Array too short");
            }
            Util.writeInt32Le(out, packet.length(), 0);
            Util.writeInt32Le(out, packet.id(), 4);
            Util.writeInt32Le(out, packet.type(), 8);
            System.arraycopy(packet.payload(), 0, out, 12, packet.payload().length);
            out[packet.length() - 2] = out[packet.length() - 1] = 0;
        }
    }

    public static ForByteArray forByteArray() {
        return ForByteArray.INSTANCE;
    }

    public static RconPacket.Reader<InputStream> forInputStream() {
        return (in) -> {
            int length = Util.readInt32Le(in);
            int id = Util.readInt32Le(in);
            int type = Util.readInt32Le(in);
            byte[] payload = new byte[length - 4 - 4 - 2];
            int realLength = in.read(payload);
            if (realLength != payload.length) {
                throw new MalformedPacketException("Excepted payload length=" + payload.length + ", real length=" + realLength);
            }
            if (in.read() != 0 || in.read() != 0) {
                throw new MalformedPacketException("Invalid packet terminators");
            }
            return Util.createS2CPacket(id, type, payload);
        };
    }

    public static RconPacket.Writer<OutputStream> forOutPutStream() {
        return (out, packet) -> {
            Util.writeInt32Le(out, packet.length());
            Util.writeInt32Le(out, packet.id());
            Util.writeInt32Le(out, packet.type());
            out.write(packet.payload());
            out.write(0);
            out.write(0);
            out.flush();
        };
    }

    public static class ForByteBuffer implements RconPacket.Writer<ByteBuffer>, RconPacket.Reader<ByteBuffer> {

        private static final ForByteBuffer INSTANCE = new ForByteBuffer();

        private ForByteBuffer() {}

        @Override
        public @NotNull RconPacket read(@NotNull ByteBuffer in) throws MalformedPacketException {
            ByteOrder originalOrder = in.order();
            in.order(ByteOrder.LITTLE_ENDIAN);

            if (in.remaining() < 4 + 4 + 4 + 2) {
                throw new MalformedPacketException("Not enough bytes");
            }

            in.mark();

            try {
                int length = in.getInt();
                int id = in.getInt();
                int type = in.getInt();

                if (in.remaining() < length - 4 - 4 - 2) {
                    throw new MalformedPacketException("Not enough bytes for payload");
                }

                byte[] payload = new byte[length - 4 - 4 - 2];
                in.get(payload);

                if (in.remaining() < 2) {
                    throw new MalformedPacketException("Missing packet terminators");
                }

                if (in.get() != 0 || in.get() != 0) {
                    throw new MalformedPacketException("Invalid packet terminators");
                }

                return Util.createS2CPacket(id, type, payload);
            } catch (BufferUnderflowException e) {
                in.reset();
                throw new MalformedPacketException("Buffer underflow: " + e.getMessage(), e);
            } finally {
                in.order(originalOrder);
            }
        }

        @Override
        public void write(@NotNull ByteBuffer out, @NotNull RconPacket packet) throws IllegalArgumentException {
            int requiredSize = 4 + packet.length();

            if (out.remaining() < requiredSize) {
                throw new IllegalArgumentException("Buffer capacity insufficient: " +
                        out.remaining() + " < " + requiredSize);
            }

            ByteOrder originalOrder = out.order();
            out.order(ByteOrder.LITTLE_ENDIAN);

            out.putInt(packet.length());
            out.putInt(packet.id());
            out.putInt(packet.type());
            out.put(packet.payload());

            out.put((byte) 0);
            out.put((byte) 0);

            out.order(originalOrder);
        }
    }

    public static ForByteBuffer forByteBuffer() {
        return ForByteBuffer.INSTANCE;
    }

    public static RconPacket.Reader<ReadableByteChannel> forReadableByteChannel() {
        return (channel) -> {
            ByteBuffer headerBuffer = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN);

            int bytesRead = 0;
            while (bytesRead < 12) {
                int count = channel.read(headerBuffer);
                if (count == -1) {
                    throw new MalformedPacketException("Channel closed before reading header");
                }
                bytesRead += count;
            }

            headerBuffer.flip();
            int length = headerBuffer.getInt();
            int id = headerBuffer.getInt();
            int type = headerBuffer.getInt();

            int payloadLength = length - 4 - 4 - 2;
            ByteBuffer payloadBuffer = ByteBuffer.allocate(payloadLength);

            bytesRead = 0;
            while (bytesRead < payloadLength) {
                int count = channel.read(payloadBuffer);
                if (count == -1) {
                    throw new MalformedPacketException("Channel closed before reading payload");
                }
                bytesRead += count;
            }

            ByteBuffer terminatorBuffer = ByteBuffer.allocate(2);
            bytesRead = 0;
            while (bytesRead < 2) {
                int count = channel.read(terminatorBuffer);
                if (count == -1) {
                    throw new MalformedPacketException("Channel closed before reading terminators");
                }
                bytesRead += count;
            }

            terminatorBuffer.flip();
            if (terminatorBuffer.get() != 0 || terminatorBuffer.get() != 0) {
                throw new MalformedPacketException("Invalid packet terminators");
            }

            return Util.createS2CPacket(id, type, payloadBuffer.array());
        };
    }

    public static RconPacket.Writer<WritableByteChannel> forWritableByteChannel() {
        return (channel, packet) -> {
            int totalLength = 4 + packet.length();
            ByteBuffer buffer = ByteBuffer.allocate(totalLength).order(ByteOrder.LITTLE_ENDIAN);

            buffer.putInt(packet.length());
            buffer.putInt(packet.id());
            buffer.putInt(packet.type());
            buffer.put(packet.payload());
            buffer.put((byte) 0);
            buffer.put((byte) 0);

            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        };
    }

}
