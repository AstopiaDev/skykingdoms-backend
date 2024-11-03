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

@WithAlias("changepass")
@NamedCommand("changepassword")
public class ChangePasswordCommand {
    @Executor
    @WithCondition("minArgs: 2")
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        String password = session.getArgument(0);
        String newPassword = session.getArgument(1);

        if (AuthManager.INSTANCE.changePassword(gamer, password, newPassword)) {
            gamer.sendLocalizedMessage("authentication_password_changed");
        } else {
            gamer.sendLocalizedMessage("authentication_password_incorrect");
        }
    }

    @WithCondition("group: owner")
    @Tabcomplete("[playerName] [void]")
    @WithCondition("minArgs: 2")
    @WithAlias("achangepass")
    @RelayExecutor("achangepassword")
    public static void adminExecutor(CoreExecutionSession session) throws CommandTranslationException {
        String playerName = session.getArgument(0);
        Gamer gamer = session.getOfflineGamer(0, Gamer::loadPlayerId);
        String password = session.getArgument(1);

        if (AuthSqlHandler.INSTANCE.isPremium(gamer.getGamerId()).join()) {
            throw new CommandTranslationException("authentication_premium_changepassword_other");
        }

        AuthManager.INSTANCE.forceChangePassword(gamer, password);
        session.getSender().sendLocalizedMessage(false, "authentication_admin_change_password", "%player%", playerName);
        if (gamer.isOnline()) {
            gamer.sendLocalizedMessage("authentication_password_changed");
        }
    }
}
