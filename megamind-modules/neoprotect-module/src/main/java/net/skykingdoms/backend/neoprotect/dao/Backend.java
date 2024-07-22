package net.skykingdoms.backend.neoprotect.dao;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter @Setter
public final class Backend {

    String id;
    @NonFinal String ip;
    int port;
    boolean geyser;

    public boolean compareById(final @NonNull String otherId) {
        return id.equals(otherId);
    }

}