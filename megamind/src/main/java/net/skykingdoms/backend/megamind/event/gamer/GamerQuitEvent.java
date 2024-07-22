package net.skykingdoms.backend.megamind.event.gamer;

import lombok.NonNull;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * 
 */
public final class GamerQuitEvent extends GamerEvent {

    public GamerQuitEvent(final @NonNull Gamer gamer) {
        super(gamer);
    }
}
