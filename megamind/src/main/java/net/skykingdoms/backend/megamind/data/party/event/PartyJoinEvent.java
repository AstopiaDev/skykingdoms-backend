package net.skykingdoms.backend.megamind.data.party.event;

import lombok.NonNull;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.data.party.Party;

/**
 * @author Vihara
 */
public final class PartyJoinEvent extends PartyEvent {

    Gamer gamer;

    public PartyJoinEvent(final @NonNull Party party,
                          final @NonNull Gamer gamer) {
        super(party);
        this.gamer = gamer;
    }
}
