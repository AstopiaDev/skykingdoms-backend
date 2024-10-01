package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * @author Vihara
 */
@WithAlias("networkbroadcast")
@NamedCommand("alert")
public final class AlertCommand {
    @WithCondition("minArgs: 1")
    @WithCondition("group: admin")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) {
        Gamer.getGamers().forEach(gamer -> {
            gamer.sendMessage(
                    "\n" +
                    ChatColor.RED + ChatColor.BOLD + "Alert: " + ChatColor.WHITE + session.getArgumentString(0) +
                    "\n");
        });
    }

}
