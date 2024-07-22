package net.skykingdoms.backend.megamind.event.gamer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.api.plugin.Event;

import java.net.InetAddress;
import java.util.UUID;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter @Setter
public final class PlayerLoginEvent extends Event implements Cancellable {

    final String playerName;
    final UUID playerUuid;
    final InetAddress address;
    final State state;

    boolean cancelled;

    String kickReason;

    public enum State {
        POST, RECONNECT
    }

}
