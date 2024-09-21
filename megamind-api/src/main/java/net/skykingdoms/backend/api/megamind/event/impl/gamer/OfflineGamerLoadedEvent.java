package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;

public class OfflineGamerLoadedEvent extends GamerEvent {

    public OfflineGamerLoadedEvent(MegamindGamer gamer) {
        super(gamer);
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
