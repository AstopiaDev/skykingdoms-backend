package net.skykingdoms.backend.protocol.type.duel;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.api.protocol.type.GameMode;

@FieldDefaults(level = AccessLevel.PROTECTED)
@Accessors(fluent = true)
@AllArgsConstructor
@Getter
@Setter
public abstract class FluentDuelRequestData implements Serializable<FluentDuelRequestData> {
    String map = "";

    public static <T extends FluentDuelRequestData> T deserialize(final @NonNull GameMode mode,
                                                final @NonNull ByteBuf buf) {

        /*
            TODO: Later implement some kind of GameMode duels realization and then check if the duel
             requires a kit or not, etc.
         */

        Logging.IMP.info("Deserializing data for mode: " + mode);

        if (mode == GameMode.DUELS_SKYPVP_DUEL) {
            return (T) new KitFluentRequestData().deserialize(buf);
        }

        return null;
    }
}
