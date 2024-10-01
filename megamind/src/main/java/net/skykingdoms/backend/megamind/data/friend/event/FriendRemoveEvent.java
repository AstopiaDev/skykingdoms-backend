package net.skykingdoms.backend.megamind.data.friend.event;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Data
public final class FriendRemoveEvent extends Event {

    Gamer sender;
    Gamer gamer;

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
