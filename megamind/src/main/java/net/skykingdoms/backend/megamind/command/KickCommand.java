package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.StaffChatManager;

/**
 * @author Vihara
 */
@NamedCommand("kick")
public final class KickCommand {

    @WithCondition("group: moderator")
    @WithCondition("minArgs: 1")
    @Tabcomplete("[playerName]")
    @Executor
    public static void execute(final CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getGamer(0);
        var reason = "Not specified";
        if (session.getArgumentCount() > 1) {
            reason = session.getArgumentString(1);
        }

        target.disconnect(reason);

        StaffChatManager.INSTANCE.sendMessage(session.getDisplayName() + ChatColor.WHITE + " kicked " + target.getDisplayName() + ChatColor.WHITE + " for " + ChatColor.ITALIC + reason);
    }

}
