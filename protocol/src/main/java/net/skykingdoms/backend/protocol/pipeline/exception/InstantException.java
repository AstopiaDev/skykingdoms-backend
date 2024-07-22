package net.skykingdoms.backend.protocol.pipeline.exception;

import lombok.NonNull;

public class InstantException extends Exception {

    public InstantException(final @NonNull String message) {
        super(message);
    }

    public InstantException(final @NonNull String message,
                            final @NonNull Throwable cause) {
        super(message, cause);
    }

    public InstantException(final @NonNull Throwable cause) {
        super(cause);
    }

    @Override
    public Throwable initCause(final @NonNull Throwable cause) {
        return this;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
