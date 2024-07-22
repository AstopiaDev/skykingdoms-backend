package net.skykingdoms.backend.megamind.data.friend.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Data
public final class FriendRemoveEvent extends Event {

    Gamer sender;
    Gamer gamer;

}
