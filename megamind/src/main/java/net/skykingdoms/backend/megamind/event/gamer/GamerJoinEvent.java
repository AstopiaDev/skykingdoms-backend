package net.skykingdoms.backend.megamind.event.gamer;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * 
 */
@Getter
public final class GamerJoinEvent extends GamerEvent {

    public GamerJoinEvent(final @NonNull Gamer player) {
        super(player);
    }
}
