package net.skykingdoms.backend.base.data.skin.exception;

import lombok.NonNull;

public abstract class SkinException extends Exception {

    protected SkinException(@NonNull String localizedMessage, @NonNull Throwable cause) {
        super(localizedMessage, cause);
    }

    protected SkinException(@NonNull String localizedMessage) {
        super(localizedMessage);
    }

    protected SkinException() {
    }

}
