package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

/**
 * 
 */
@Getter
public final class GamerJoinEvent extends GamerEvent {
    public GamerJoinEvent(final MegamindGamer player) {
        super(player);
    }

    @Override
    public GamerJoinEvent serialize(@NonNull ByteBuf out) {
        this.getGamer().serialize(out);
        return this;
    }

    @Override
    public GamerJoinEvent deserialize(@NonNull ByteBuf in) {

/*
        this.setGamer(AbstractGamer.getGamer());
*/
        return this;
    }
}
