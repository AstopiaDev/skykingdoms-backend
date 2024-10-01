package net.skykingdoms.api.megamind.event.impl.core;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.megamind.event.Event;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public class RemoteClientCommandExecuteEvent extends Event {

    ICommandSender sender;
    // AbstractServer server;
    String command;

    @Override
    public Event serialize(@NonNull ByteBuf out) {
        return null;
    }

    @Override
    public Event deserialize(@NonNull ByteBuf in) {
        return null;
    }
}
