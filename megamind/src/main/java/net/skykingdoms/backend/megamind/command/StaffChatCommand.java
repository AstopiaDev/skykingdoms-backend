package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.StaffChatManager;

/**
 * @author Vihara
 */
@WithAlias("sc")
@NamedCommand("staffchat")
public final class StaffChatCommand {

    @WithCondition("group: jr_helper")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final CoreExecutionSession session) {
        StaffChatManager.INSTANCE.sendMessage(session.getGamer().getColoredName() + ChatColor.WHITE + ": " + session.getArgumentString(0));
    }

}
