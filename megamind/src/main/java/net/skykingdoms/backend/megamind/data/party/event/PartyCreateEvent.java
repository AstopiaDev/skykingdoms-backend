package net.skykingdoms.backend.megamind.data.party.event;

import lombok.NonNull;
import net.skykingdoms.backend.megamind.data.party.Party;

/**
 * @author Vihara
 */
public final class PartyCreateEvent extends PartyEvent {

    public PartyCreateEvent(final @NonNull Party party) {
        super(party);
    }
}
