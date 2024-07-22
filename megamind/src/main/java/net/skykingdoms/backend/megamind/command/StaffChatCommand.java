package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.StaffChatManager;

/**
 * @author Vihara
 */
@WithAlias("sc")
@NamedCommand("staffchat")
public final class StaffChatCommand {

    @WithCondition("group: helper")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final CoreExecutionSession session) {
        StaffChatManager.INSTANCE.sendMessage(session.getDisplayName() + ChatColor.WHITE + ": " + session.getArgumentString(0));
    }

}
