package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;

/**
 * 
 */
public final class GamerLoadEvent extends GamerEvent {

    public GamerLoadEvent(MegamindGamer player) {
        super(player);
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
