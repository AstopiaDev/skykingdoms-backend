package net.skykingdoms.backend.megamind.event.gamer;

import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * 
 */
public final class GamerLoadEvent extends GamerEvent {

    public GamerLoadEvent(Gamer player) {
        super(player);
    }

}
