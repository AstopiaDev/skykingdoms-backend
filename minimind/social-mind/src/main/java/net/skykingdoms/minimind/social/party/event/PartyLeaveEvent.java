package net.skykingdoms.minimind.social.party.event;

import lombok.NonNull;
import net.skykingdoms.minimind.social.SocialGamer;
import net.skykingdoms.minimind.social.party.Party;

/**
 * @author Vihara
 */
public final class PartyLeaveEvent extends PartyEvent {

    SocialGamer gamer;

    public PartyLeaveEvent(final @NonNull Party party,
                           final @NonNull SocialGamer gamer) {
        super(party);
        this.gamer = gamer;
    }
}
