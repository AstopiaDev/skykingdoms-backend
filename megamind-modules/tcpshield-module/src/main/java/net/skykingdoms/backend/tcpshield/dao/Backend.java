package net.skykingdoms.backend.tcpshield.dao;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter @Setter
public final class Backend {
    @NonFinal String ip;
    int port;
}