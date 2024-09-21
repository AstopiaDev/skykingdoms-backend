package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;

/**
 * 
 */
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class GamerEvent extends Event {
    @NonFinal MegamindGamer gamer;
}
