package moe.mukjep.rcon4j

import io.ktor.utils.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

suspend fun ByteReadChannel.readRconPacket(): RconPacket {
    val header = ByteArray(4 * 3)
    readFully(header)
    readInt()
    val buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN)

    val length = buffer.getInt()
    val id = buffer.getInt()
    val type = buffer.getInt()

    val bodyBytes = ByteArray(length - 4 - 4 - 2)
    readFully(bodyBytes)
    discard(2)
    return RconPacket(id, type, bodyBytes)
}
