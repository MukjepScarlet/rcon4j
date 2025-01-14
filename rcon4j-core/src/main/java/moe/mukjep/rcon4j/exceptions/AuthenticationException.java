package moe.mukjep.rcon4j.exceptions;

import org.jetbrains.annotations.Nullable;

public final class AuthenticationException extends Exception {
    public AuthenticationException(@Nullable String message) {
        super(message);
    }
}
