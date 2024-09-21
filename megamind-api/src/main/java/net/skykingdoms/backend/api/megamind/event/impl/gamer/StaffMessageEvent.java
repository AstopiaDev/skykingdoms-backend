package net.skykingdoms.backend.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.language.LanguageContext;
import net.skykingdoms.backend.api.megamind.event.Cancellable;
import net.skykingdoms.backend.api.megamind.event.Event;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Getter
@Setter
public final class StaffMessageEvent extends Event implements Cancellable {

    final LanguageContext context;
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
