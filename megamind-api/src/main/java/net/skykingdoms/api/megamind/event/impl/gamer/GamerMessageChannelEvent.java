package net.skykingdoms.api.megamind.event.impl.gamer;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class GamerMessageChannelEvent extends GamerEvent {
    IMegamindGamer gamer;
    int channelId;
    String message;

    public GamerMessageChannelEvent(IMegamindGamer gamer, int channelId, String message) {
        super(gamer);
        this.channelId = channelId;
        this.message = message;
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
