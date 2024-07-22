package net.skykingdoms.backend.megamind.event.gamer;

import net.skykingdoms.backend.megamind.data.gamer.Gamer;

public class OfflineGamerLoadedEvent extends GamerEvent {

    public OfflineGamerLoadedEvent(Gamer gamer) {
        super(gamer);
    }
}
