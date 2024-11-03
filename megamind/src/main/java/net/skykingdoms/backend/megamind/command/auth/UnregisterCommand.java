package net.skykingdoms.backend.megamind.command.auth;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.RelayExecutor;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.data.auth.AuthSqlHandler;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.auth.AuthManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@WithAlias("unreg")
@NamedCommand("unregister")
public class UnregisterCommand {
    @Executor
    @WithCondition("minArgs: 1")
    public static void executor(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        String password = session.getArgument(0);

        if (!gamer.isRegistered()) {
            gamer.sendLocalizedMessage("authentication_not_registered");
            return;
        }

        if (!gamer.isLoggedIn()) {
            gamer.sendLocalizedMessage("authentication_not_logged_in");
        }

        if (AuthManager.INSTANCE.unregisterGamer(gamer, password)) {
            gamer.sendLocalizedMessage("authentication_unregistered");
        } else {
            gamer.sendLocalizedMessage("authentication_password_incorrect");
        }
    }

    @WithCondition("group: owner")
    @Tabcomplete("[playerName]")
    @WithCondition("minArgs: 1")
    @WithAlias("aunreg")
    @RelayExecutor("aunregister")
    public static void adminExecutor(CoreExecutionSession session) throws CommandTranslationException {
        String playerName = session.getArgument(0);
        Gamer gamer = session.getOfflineGamer(0, Gamer::loadPlayerId);

        if (AuthSqlHandler.INSTANCE.isPremium(gamer.getGamerId()).join()) {
            throw new CommandTranslationException("authentication_premium_unregister_other");
        }

        if (gamer.isRegistered()) {
            gamer.sendLocalizedMessage("authentication_already_registered");
            return;
        }

        AuthManager.INSTANCE.forceUnregisterGamer(gamer);
        session.getSender().sendLocalizedMessage(false, "authentication_admin_unregistered", "%player%", playerName);
        if (gamer.isOnline()) {
            gamer.sendLocalizedMessage("authentication_unregistered");
        }
    }
}
