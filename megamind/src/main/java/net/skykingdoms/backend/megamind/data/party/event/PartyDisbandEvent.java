package net.skykingdoms.backend.megamind.data.party.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.data.party.Party;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class PartyDisbandEvent extends PartyEvent {

    DisbandReason disbandReason;

    public PartyDisbandEvent(final @NonNull Party party,
                             final @NonNull DisbandReason reason) {
        super(party);
        this.disbandReason = reason;
    }

    public enum DisbandReason {
        LEADER_DISBANDED, EVERYONE_LEFT
    }
}
