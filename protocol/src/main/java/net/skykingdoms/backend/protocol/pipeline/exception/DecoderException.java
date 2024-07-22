package net.skykingdoms.backend.protocol.pipeline.exception;

import lombok.NonNull;

public final class DecoderException extends InstantException {

    public DecoderException(final @NonNull String message) {
        super(message);
    }

    public DecoderException(final @NonNull Throwable cause) {
        super(cause);
    }

}
