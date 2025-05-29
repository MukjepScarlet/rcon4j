# rcon4j [![](https://jitpack.io/v/MukjepScarlet/rcon4j.svg)](https://jitpack.io/#MukjepScarlet/rcon4j)

RCON protocol and client implementation for Java (8+) and Kotlin (JVM/2.0+).

## Modules

### [rcon4j-core](./rcon4j-core)

Info:
- Language: Java

Content:
- Packet definitions:
  - `RconPacket.Auth`, `RconPacket.ExecCommand`, `RconPacket.AuthResponse`, `RconPacket.ResponseValue`
- Packet adapter interface:
  - `Writer<T>`: write packet instance into `output: T`.
  - `Reader<T>`: read packet instance from `input: T`.
  - Built-in adapter implementations:
    - Basic: `byte[]`(I/O), `InputStream`, `OutputStream`
    - `java.nio`: `ByteBuffer`, `ReadableByteChannel`, `WritableByteChannel`
- Client interface:
  - `connect(InetAddress, int)` / `connect(String, int)`
  - `RconPacket send(RconPacket)`
  - Default method `authenticate` (for `RconPacket.Auth`) and `command` (for `RconPacket.ExecCommand`)
  - Default implementation: based on `java.io` and `Socket`
- Exceptions:
  - `MalformedPacketException` (`extends IOException`)

### [rcon4j-netty](./rcon4j-netty)

Adaptation for [Netty](https://github.com/netty/netty), more granular control over the network. Asynchronously.

Info:
- Language: Java

Content:
- Packet adapter implementation for `ByteBuf`
- `RconPacketDecoder` (`extends ByteToMessageDecoder`)
- `RconPacketEncoder` (`extends MessageToByteEncoder<RconPacket>`)

### [rcon4j-okio](./rcon4j-okio)

If you are already using [OkHttp](https://github.com/square/okhttp) or [Okio](https://github.com/square/okio), please use this.

Info:
- Language: Kotlin (Kotlin & Java)

Content:
- Packet adapter implementation for `BufferedSource` and `BufferedSink`
- `OkioRconClient`: Based on `Socket`, using `BufferedSource` and `BufferedSink` for IO.

### [rcon4j-ktor](./rcon4j-ktor)

Adaptation for [Ktor](https://github.com/ktorio/ktor). Suspend.

Info:
- Language: Kotlin (Kotlin-only)

Content:
- Write packet to `ByteWriteChannel`, read packet from `ByteReadChannel` (with extensions)
- `KtorRconClient`, based on Ktor's TCP Socket.
- `KtorRconServer` (**experimental**)

### [rcon4j-testing](./rcon4j-testing)

Info:
- Language: Kotlin
- Not a maven lib

Content:
- Netty RCON server for testing

## Protocol reference

- [Valve Wiki](https://developer.valvesoftware.com/wiki/Source_RCON_Protocol)
- [Minecraft Wiki](https://minecraft.wiki/w/RCON)
- [Python Valve doc](https://python-valve.readthedocs.io/en/latest/rcon.html)
