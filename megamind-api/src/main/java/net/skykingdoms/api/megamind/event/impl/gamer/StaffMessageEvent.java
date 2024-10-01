package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.language.ILanguageContext;
import net.skykingdoms.api.megamind.event.Cancellable;
import net.skykingdoms.api.megamind.event.Event;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Setter
public final class StaffMessageEvent extends Event implements Cancellable {

    final ILanguageContext context;
    boolean cancelled;

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
