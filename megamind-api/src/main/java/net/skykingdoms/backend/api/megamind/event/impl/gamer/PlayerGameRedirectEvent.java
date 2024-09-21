package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class PlayerGameRedirectEvent extends Event {

    private final MegamindGamer gamer;

    private final List<MegamindGamer> additionalGamerList = new ArrayList<>();

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
