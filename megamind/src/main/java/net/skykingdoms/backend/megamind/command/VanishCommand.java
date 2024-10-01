package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.megamind.event.impl.gamer.PlayerVanishEvent;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.server.AbstractScalable;

@WithAlias("v")
@NamedCommand("vanish")
public final class VanishCommand {

    @Executor
    @WithCondition("group: moderator")
    public static void execute(CoreExecutionSession session) throws CommandTranslationException {
        Gamer sender = session.getGamer();

        boolean vanished = !sender.isVanished();

        if (((AbstractScalable) sender.getCurrentServer()).getServerScale() != ServerScaleType.LOBBY && ((AbstractScalable) sender.getCurrentServer()).getServerScale() != ServerScaleType.PERSISTENT) {
            throw new CommandTranslationException("core_vanish_lobbyonly");
        }

        if (sender.setVanished(vanished)) {
            session.sendLocalizedMessage(vanished ? "core_vanish_enabled" : "core_vanish_disabled");
        }

        MegamindApp.INSTANCE.pluginManager().callEvent(new PlayerVanishEvent(sender, vanished));
    }
}
