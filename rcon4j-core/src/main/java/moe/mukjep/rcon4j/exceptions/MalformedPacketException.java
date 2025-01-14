package moe.mukjep.rcon4j.exceptions;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class MalformedPacketException extends IOException {
    public MalformedPacketException(@Nullable String message) {
        super(message);
    }
}
