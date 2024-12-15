package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Cancellable;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class MessageEvent extends Event implements Cancellable {

    final IMegamindGamer from;
    final IMegamindGamer to;

    @Setter String message;
    @Setter boolean cancelled;

    public MessageEvent(final @NonNull IMegamindGamer from,
                        final @NonNull IMegamindGamer to,
                        final @NonNull String msg) {
        this.from = from;
        this.to = to;
        this.message = msg;
    }

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
