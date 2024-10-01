package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.DeploymentManager;

/**
 * Basically shuts down a server with some benefits.
 * Eg: "force" flag indicates that we send a destroy packet to
 * @author Vihara
 */
@NamedCommand("destroyinstance")
public final class DestroyInstanceCommand {

    @WithCondition("group: admin")
    
    @WithCondition("minArgs: 1")
    @Tabcomplete("[remote]")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) {
        val name = session.getArgument(0);
        var force = false;
        var cocked = false;

        for (val arg : session.getArguments()) {
            if (arg.equalsIgnoreCase("-f")) force = true;
            if (arg.equalsIgnoreCase("-c")) cocked = true;
        }

        session.sendMessage("Destroying remote client: {} - Flags: F({}) / C({})", name, force, cocked);

        DeploymentManager.INSTANCE.sendInstanceRemoveCommand(name, force, cocked, (a) -> {
            switch (a) {
                case FORCE_DESTRUCTED: {
                    session.sendMessage("Successfully received confirmation packet by node {}", a.getServer().getName());
                    return;
                }
                case NO_FORCED_REMOTE: {
                    session.sendMessage("No remote client with name {} recursively found on the network!", name);
                    return;
                }
                case TRANSACTION_FAILED: {
                    session.sendMessage("Transaction failed!");
                    return;
                }
                case NO_UNFORCED_REMOTE: {
                    session.sendMessage("Couldn't find a remote client by this name. Try \"-f\" flag");
                    return;
                }
                case DESTRUCTED_EXISTENT: {
                    session.sendMessage("Successfully destructed registered client: {}", name);
                }
            }
        });
    }

}
