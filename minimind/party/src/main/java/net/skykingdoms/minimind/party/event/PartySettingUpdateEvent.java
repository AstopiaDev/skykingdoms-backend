package net.skykingdoms.minimind.party.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.data.party.Party;
import net.skykingdoms.backend.megamind.data.party.event.PartyEvent;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class PartySettingUpdateEvent extends PartyEvent {

    Party.Setting setting;

    boolean oldValue;

    boolean newValue;

    public PartySettingUpdateEvent(final @NonNull Party party,
                                   final @NonNull Party.Setting setting,
                                   final boolean oldValue,
                                   final boolean newValue) {
        super(party);
        this.setting = setting;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
