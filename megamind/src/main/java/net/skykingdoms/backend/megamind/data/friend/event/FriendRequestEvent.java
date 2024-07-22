package net.skykingdoms.backend.megamind.data.friend.event;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = true)
@Data
public final class FriendRequestEvent extends Event implements Cancellable {

    Gamer sender;
    Gamer gamer;
    @NonFinal boolean cancelled;
}
