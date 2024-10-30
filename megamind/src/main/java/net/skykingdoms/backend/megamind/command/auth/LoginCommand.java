package net.skykingdoms.backend.megamind.command.auth;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.WithMultipleAliases;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.auth.AuthManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@WithMultipleAliases(
        {
                @WithAlias("l"),
                @WithAlias("signin")
        }
)
@NamedCommand("login")
public class LoginCommand {
    @Executor
    @WithCondition("noAuth")
    @WithCondition("minArgs: 1")
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        String password = session.getArgument(0);

        if (gamer.isLoggedIn()) {
            gamer.sendLocalizedMessage("authentication_already_logged_in");
            return;
        }

        if (!gamer.isRegistered()) {
            gamer.sendLocalizedMessage("authentication_not_registered");
            return;
        }

        if (AuthManager.INSTANCE.loginGamer(gamer, password)) {
            gamer.setLoggedIn(true);
            gamer.sendLocalizedMessage("authentication_logged_in");
        } else {
            gamer.sendLocalizedMessage("authentication_password_incorrect");
        }
    }
}
