package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.api.megamind.event.Cancellable;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter @Setter
public final class GamerProxyLoginEvent extends GamerEvent implements Cancellable {

    final boolean reconnect;

    boolean cancelled;
    String kickReason;

    public GamerProxyLoginEvent(final MegamindGamer player,
                                final boolean reconnect) {
        super(player);
        this.reconnect = reconnect;
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
