package moe.lasoleil.rcon4j.exceptions;

import org.jetbrains.annotations.Nullable;

public class AuthenticationException extends Exception {
    public AuthenticationException(@Nullable String message) {
        super(message);
    }
}
