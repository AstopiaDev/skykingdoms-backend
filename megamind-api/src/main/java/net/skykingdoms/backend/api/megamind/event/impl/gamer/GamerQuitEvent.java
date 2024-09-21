package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;
import net.skykingdoms.backend.protocol.packet.Packet;

/**
 * 
 */
public final class GamerQuitEvent extends GamerEvent {

    public GamerQuitEvent(final @NonNull MegamindGamer gamer) {
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
