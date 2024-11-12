package net.skykingdoms.backend.megamind.command.auth;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.WithMultipleAliases;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.util.hash.HashType;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.auth.AuthManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@WithMultipleAliases(
        {
                @WithAlias("reg"),
                @WithAlias("signup")
        }
)
@NamedCommand("register")
public class RegisterCommand {
    @Executor
    @WithCondition("noAuth")
    @WithCondition("minArgs: 2")
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        String password = session.getArgument(0);
        String confirmPassword = session.getArgument(1);

        if (gamer.isRegistered()) {
            gamer.sendLocalizedMessage("authentication_already_registered");
            return;
        }

        if (!password.equals(confirmPassword)) {
            gamer.sendLocalizedMessage("authentication_password_no_match");
            return;
        }

        AuthManager.INSTANCE.registerGamer(gamer, password);
        gamer.sendLocalizedMessage("authentication_registered");
    }
}
