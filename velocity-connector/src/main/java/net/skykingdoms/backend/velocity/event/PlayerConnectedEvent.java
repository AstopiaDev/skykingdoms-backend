package net.skykingdoms.backend.velocity.event;

import com.velocitypowered.api.event.Continuation;
import com.velocitypowered.api.proxy.InboundConnection;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode(callSuper = false)
@ToString
@Getter
public final class PlayerConnectedEvent {

    InboundConnection connection;
    Continuation continuation;

    public PlayerConnectedEvent(final @NonNull InboundConnection con, Continuation continuation) {
        this.connection = con;
        this.continuation = continuation;
    }
}
