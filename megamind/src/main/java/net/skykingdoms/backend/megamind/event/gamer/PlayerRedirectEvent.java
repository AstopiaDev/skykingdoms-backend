package net.skykingdoms.backend.megamind.event.gamer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.instance.AbstractPlayerHolder;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class PlayerRedirectEvent extends Event implements Cancellable {

    final Gamer gamer;

    final AbstractPlayerHolder target;

    @Getter @Setter private boolean cancelled;

    public PlayerRedirectEvent(final @NonNull Gamer gamer,
                               final @NonNull AbstractPlayerHolder target) {
        this.gamer = gamer;
        this.target = target;
    }

}