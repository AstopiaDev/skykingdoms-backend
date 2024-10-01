package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

/**
 * 
 */
public final class GamerLoadEvent extends GamerEvent {

    public GamerLoadEvent(IMegamindGamer player) {
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
