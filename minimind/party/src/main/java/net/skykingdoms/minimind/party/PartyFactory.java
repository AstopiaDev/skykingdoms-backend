package net.skykingdoms.minimind.party;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import lombok.val;
import net.skykingdoms.backend.megamind.data.party.Party;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Vihara
 */
public interface PartyFactory {

    /**
     * Used to get ID for new party
     */
    AtomicInteger PARTY_ID_COUNTER = new AtomicInteger(0);

    // ------------------------------------------------------------------------

    /**
     * Creates a new party for only one player - leader
     */
    static @NotNull Party bakeParty(final int leaderId) {
        return new Party(PARTY_ID_COUNTER.incrementAndGet(), leaderId);
    }

    /**
     * Creates a new filled party.
     * Commonly used on party restore process.
     */
    static @NotNull Party bakeRestoredParty(final int leaderId,
                                            final int settings,
                                            final int[] kickedGamers,
                                            final int[] members,
                                            final int[] moderators) {
        val party = new Party(PARTY_ID_COUNTER.incrementAndGet(), leaderId);
        party.setSettings(settings);
        party.getKickedGamers().addAll(new IntOpenHashSet(kickedGamers));

        // adding all members and leader
        for (val member : members) {
            party.addPartyMember(member, Party.PartyRole.MEMBER);
        }

        for (val moderator : moderators) {
            party.addPartyMember(moderator, Party.PartyRole.MODERATOR);
        }

        return party;
    }

}
