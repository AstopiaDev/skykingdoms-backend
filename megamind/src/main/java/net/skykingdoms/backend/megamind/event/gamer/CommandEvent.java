package net.skykingdoms.backend.megamind.event.gamer;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@ToString
@Getter
public final class CommandEvent extends Event implements Cancellable {

    Gamer sender;
    String command;
    String[] args;

    @Setter @NonFinal boolean cancelled;

}
