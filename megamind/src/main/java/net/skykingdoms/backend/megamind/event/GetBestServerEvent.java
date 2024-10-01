package net.skykingdoms.backend.megamind.event;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;

import java.net.InetAddress;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Setter
@Getter
public class GetBestServerEvent extends Event {

    String name;
    InetAddress address;

    @NonFinal
    AbstractPlayerHolder bestServer;

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
