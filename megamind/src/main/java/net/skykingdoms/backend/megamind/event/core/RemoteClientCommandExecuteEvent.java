package net.skykingdoms.backend.megamind.event.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.command.CommandSender;
import net.skykingdoms.backend.megamind.api.plugin.Event;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public class RemoteClientCommandExecuteEvent extends Event {

    CommandSender sender;
    // AbstractServer server;
    String command;

}
