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

@NamedCommand("logout")
public class LogoutCommand {
    @Executor
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();

        if (!gamer.isLoggedIn()) {
            gamer.sendLocalizedMessage("authentication_not_logged_in");
            return;
        }

        if (!gamer.isRegistered()) {
            gamer.sendLocalizedMessage("authentication_not_registered");
            return;
        }

        if (gamer.isPremium()) {
            gamer.sendLocalizedMessage("authentication_premium_logout");
            return;
        }

        if (AuthManager.INSTANCE.logoutGamer(gamer)) {
            gamer.sendLocalizedMessage("authentication_logged_out");
        } else {
            gamer.sendLocalizedMessage("authentication_not_logged_in");
        }
    }

    @WithCondition("group: owner")
    @Tabcomplete("[playerName]")
    @WithCondition("minArgs: 1")
    @WithAlias("alogout")
    @RelayExecutor("alogout")
    public static void adminExecutor(CoreExecutionSession session) throws CommandTranslationException {
        String playerName = session.getArgument(0);
        Gamer gamer = session.getOfflineGamer(0, Gamer::loadPlayerId);

        if (AuthSqlHandler.INSTANCE.isPremium(gamer.getGamerId()).join()) {
            throw new CommandTranslationException("authentication_premium_logout_other");
        }

        if (AuthManager.INSTANCE.logoutGamer(gamer)) {
            session.getSender().sendLocalizedMessage(false, "authentication_admin_logged_out", playerName);
            if (gamer.isOnline()) {
                gamer.sendLocalizedMessage("authentication_logged_out");
            }
        } else {
            session.getSender().sendLocalizedMessage(false, "authentication_admin_not_logged_in", playerName);
        }
    }
}
