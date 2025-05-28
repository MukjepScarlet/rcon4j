package moe.lasoleil.rcon4j.exceptions;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class MalformedPacketException extends IOException {
    public MalformedPacketException(@Nullable String message) {
        super(message);
    }
}
