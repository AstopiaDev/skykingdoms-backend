package net.skykingdoms.backend.megamind.data.message;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Cancellable;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public final class MessageEvent extends Event implements Cancellable {

    final Gamer from;
    final Gamer to;

    @Setter String message;
    @Setter boolean cancelled;

    public MessageEvent(final @NonNull Gamer from,
                        final @NonNull Gamer to,
                        final @NonNull String msg) {
        this.from = from;
        this.to = to;
        this.message = msg;
    }

}
