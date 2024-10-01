package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Cancellable;
import net.skykingdoms.api.megamind.event.Event;

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

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }

    public enum State {
        POST, RECONNECT
    }

}
