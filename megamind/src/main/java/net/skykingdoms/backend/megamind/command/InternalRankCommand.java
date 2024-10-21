package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

/**
 * @author Vihara
 */
@NamedCommand("internalrankupdate")
public class InternalRankCommand {
    @Executor
    //@Tabcomplete("[playerName] [rank]")
    @WithCondition("group: owner")
    @WithCondition("minArgs: 3")
    public static void execute(final CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);
        val rank = Group.fromLegacyGroup(session.getArgument(1));
        val operation = session.getArgument(2);
        switch (operation.toLowerCase()) {
            case "add": {
                session.sendMessage(ChatColor.GRAY + "Internally added {} " + ChatColor.GRAY + "to {}",
                        rank.getDisplayName(), target.getName());
                target.addRank(rank, null);
                break;
            }
            case "remove": {
                val cock = (UserRanks) target.getRanks();
                if (!cock.canApply(rank)) {
                    session.sendMessage("{}" + ChatColor.GRAY + " didn't have {} in their ranks!", target.getName(), rank.getDisplayName());
                    return;
                }

                session.sendMessage(ChatColor.GRAY + "Internally removed {} " + ChatColor.GRAY + "from {}",
                        rank.getDisplayName(), target.getName());

                //noinspection DataFlowIssue
                target.removeRank(cock.getAllRanks().stream().filter(r -> r.getGroup() == rank).findFirst().orElse(null));
                break;
            }
            default: {
                session.sendMessage(ChatColor.GRAY + "dolbaeb nice try");
            }
        }
    }

}
