package net.skykingdoms.backend.megamind.event.gamer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter @Setter
public final class GamerProxyLoginEvent extends GamerEvent implements Cancellable {

    final boolean reconnect;

    boolean cancelled;
    String kickReason;

    public GamerProxyLoginEvent(final Gamer player,
                                final boolean reconnect) {
        super(player);
        this.reconnect = reconnect;
    }

}
