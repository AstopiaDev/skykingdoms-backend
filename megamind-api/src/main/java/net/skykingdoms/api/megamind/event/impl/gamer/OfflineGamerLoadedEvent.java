package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

public class OfflineGamerLoadedEvent extends GamerEvent {

    public OfflineGamerLoadedEvent(IMegamindGamer gamer) {
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
