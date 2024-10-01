package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.megamind.event.Cancellable;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@ToString
@Getter
public final class CommandEvent extends Event implements Cancellable {

    IMegamindGamer sender;
    String command;
    String[] args;

    @Setter @NonFinal boolean cancelled;

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
