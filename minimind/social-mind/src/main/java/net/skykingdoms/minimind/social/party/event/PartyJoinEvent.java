package net.skykingdoms.minimind.social.party.event;

import lombok.NonNull;
import net.skykingdoms.minimind.social.SocialGamer;
import net.skykingdoms.minimind.social.party.Party;

/**
 * @author Vihara
 */
public final class PartyJoinEvent extends PartyEvent {

    SocialGamer gamer;

    public PartyJoinEvent(final @NonNull Party party,
                          final @NonNull SocialGamer gamer) {
        super(party);
        this.gamer = gamer;
    }
}
