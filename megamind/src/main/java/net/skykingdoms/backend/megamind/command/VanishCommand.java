package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.event.gamer.PlayerVanishEvent;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.protocol.server.ServerScaleType;

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
