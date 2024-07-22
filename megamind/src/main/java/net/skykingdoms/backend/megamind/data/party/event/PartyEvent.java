package net.skykingdoms.backend.megamind.data.party.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.party.Party;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@Getter
public abstract class PartyEvent extends Event {

    Party party;

}
