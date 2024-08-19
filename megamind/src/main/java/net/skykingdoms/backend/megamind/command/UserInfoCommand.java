package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.permission.Group;
import net.skykingdoms.backend.base.permission.PermissionType;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * @author Vihara
 */
@NamedCommand("userinfo")
public final class UserInfoCommand {

    @Tabcomplete("[playerName]")
    @WithCondition("group: owner")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) throws CommandTranslationException {
        val gamer = session.getOfflineGamer(0, Gamer::load);
        session.sendLocalizedMessage("core_userinfo",
                "%username%", gamer.getDisplayName(),
                "%uuid%", gamer.getPlayerUniqueId(),
                "%domain%", gamer.isOnline() ? gamer.getDomain() : ChatColor.RED + "offline",
                "%address%", gamer.isOnline() ? gamer.getAddress().getHostAddress() : ChatColor.RED + "offline",
                "%rank%", gamer.getFormattedRank(gamer.getRanks().getPriorityRank().getGroup(), false) + ChatColor.WHITE + (gamer.getRanks().getPriorityRank(PermissionType.DEFAULT).getGroup() == Group.DEFAULT ? "" : " (" + gamer.getFormattedRank(gamer.getRanks().getPriorityRank(PermissionType.DEFAULT).getGroup(), false) + ChatColor.WHITE + ")"),
/*
                "%fakerank%", gamer.getFakeUserRank() == null ? ChatColor.RED + "N/A" : gamer.getFakeUserRank().getName(),
*/
                "%network%", gamer.isOnline() ? gamer.getProxyInstance().getName() : ChatColor.RED + "offline",
                "%instance%", gamer.isOnline() ? gamer.getCurrentServer() != null ? gamer.getCurrentServer().getName() : "ERR" : ChatColor.RED + "offline",
                "%firstLogin%", gamer.getFirstLogin());
    }

}
