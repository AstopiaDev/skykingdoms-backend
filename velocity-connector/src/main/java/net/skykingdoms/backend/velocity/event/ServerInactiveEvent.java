package net.skykingdoms.backend.velocity.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.packet.BossProcessor;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class ServerInactiveEvent {

    BossProcessor processor;

}
