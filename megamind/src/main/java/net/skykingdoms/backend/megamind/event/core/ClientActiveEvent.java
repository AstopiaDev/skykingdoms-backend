package net.skykingdoms.backend.megamind.event.core;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Event;
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

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
