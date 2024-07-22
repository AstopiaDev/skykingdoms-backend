package net.skykingdoms.backend.megamind.event.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.protocol.packet.BossProcessor;

/**
 * 
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ClientActiveEvent extends Event {

    BossProcessor processor;

    public ClientActiveEvent(final @NonNull BossProcessor processor) {
        this.processor = processor;
    }

}
