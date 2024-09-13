package net.skykingdoms.minimind.social.party.event;

import lombok.NonNull;
import net.skykingdoms.minimind.social.party.Party;

/**
 * @author Vihara
 */
public final class PartyCreateEvent extends PartyEvent {

    public PartyCreateEvent(final @NonNull Party party) {
        super(party);
    }
}
