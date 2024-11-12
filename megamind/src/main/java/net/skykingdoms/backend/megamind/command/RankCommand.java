package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

@NamedCommand("rank")
public class RankCommand {
    @Executor
    @WithCondition("group: owner")
    public static void execute(CoreExecutionSession session) {
        session.sendMessage("Not enough args");
    }

    // rank set Vihara owner
    @WithCondition("group: owner")
    @WithCondition("minArgs: 2")
    @WithCondition("antiDolbaeb")
    @Tabcomplete("[playerName] [rank]")
    @SubcommandExecutor("set")
    public static void setExecute(CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);
        val rank = Group.fromLegacyGroup(session.getArgument(1));

        target.setRank(rank, null);
        session.sendMessage("§aSet {}'s rank to {}", target.getName(), rank.getDisplayName());
    }

    // rank add Vihara owner
    @WithCondition("group: owner")
    @WithCondition("minArgs: 2")
    @WithCondition("antiDolbaeb")
    @Tabcomplete("[playerName] [rank]")
    @SubcommandExecutor("add")
    public static void addExecute(CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);
        val rank = Group.fromLegacyGroup(session.getArgument(1));

        target.addRank(rank, null);
        session.sendMessage("§aAdded {} §arank to {}", rank.getDisplayName(), target.getName());
    }

    // rank remove Vihara owner
    @WithCondition("group: owner")
    @WithCondition("minArgs: 2")
    @WithCondition("antiDolbaeb")
    @Tabcomplete("[playerName] [rank]")
    @SubcommandExecutor("remove")
    public static void removeExecute(CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);
        val rank = Group.fromLegacyGroup(session.getArgument(1));


        val cock = (UserRanks) target.getRanks();
        if (!cock.canApply(rank)) {
            session.sendMessage("{}" + ChatColor.GRAY + " didn't have {} in their ranks!", target.getName(), rank.getDisplayName());
            return;
        }

        target.removeRank(cock.getAllRanks().stream().filter(r -> r.getGroup() == rank).findFirst().orElse(null));
        session.sendMessage("§aRemoved {} §arank from {}", rank.getDisplayName(), target.getName());
    }
}
