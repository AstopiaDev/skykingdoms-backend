package net.skykingdoms.api.megamind.event.impl.gamer;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

/**
 * 
 */
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class GamerEvent extends Event {
    @NonFinal
    IMegamindGamer gamer;
}
