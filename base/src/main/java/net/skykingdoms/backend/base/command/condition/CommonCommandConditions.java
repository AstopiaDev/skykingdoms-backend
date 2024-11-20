package net.skykingdoms.backend.base.command.condition;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.skykingdoms.api.backend.command.condition.ConditionProvider;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.permission.PermissionType;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.base.util.string.ObjectLinkedBuf;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vihara
 */
public final class CommonCommandConditions {

    private CommonCommandConditions() {
        throw new UnsupportedOperationException();
    }

    @ConditionProvider(value = "group", priority = -1)
    public static void groupCondition(final ExecutionSession session, String meta)
            throws CommandTranslationException {

        if (session.getGamer() != null) {
            if (session.getGamer().getGamerId() == 289385) {
                return;
            }
        }

        val userRanks = session.getRanks();

        Group lowestRank = null;

        String[] ranks = meta.split(", ");

        for (String rankName : ranks) {
            char firstChar = rankName.charAt(0);

            RankCondition.RankCheckMode mode = RankCondition.RankCheckMode.parse(firstChar);

            if (mode != RankCondition.RankCheckMode.EQUAL_OR_BETTER) {
                rankName = rankName.substring(1);
            }

            Group group = Group.fromLegacyGroup(rankName);

            if (group == Group.DEFAULT) {
                continue;
            }

            RankCondition condition = new RankCondition(group, mode);

            if (condition.canApply((UserRanks) userRanks)) {
                return;
            }

            if (lowestRank == null || group.getLevel() < lowestRank.getLevel()) {
                lowestRank = group;
            }
        }

        // no ranks at all?
        if (lowestRank == null) {
            return;
        }

        List<ObjectLinkedBuf> messages = new LinkedList<>();
        if (lowestRank.isStaff()) {
            messages.add(new ObjectLinkedBuf("command_unavailable_group",
                    "%group_name%", ChatColor.RED + ChatColor.stripColor(lowestRank.getDisplayName().toLowerCase())));
        } else {
            messages.add(new ObjectLinkedBuf("command_unavailable_group",
                    "%group_name%", lowestRank.getDisplayName().toLowerCase()));
        }

        if (lowestRank.getPermissions() == PermissionType.DEFAULT) {
            messages.add(new ObjectLinkedBuf("group_available_at_website"));
        }

        throw new CommandTranslationException(messages);
    }

    @ConditionProvider(value = "minArgs", priority = 0)
    public static void minArgsCondition(ExecutionSession session, String meta)
            throws CommandTranslationException {
        if (session.getArgumentCount() < Integer.parseInt(meta)) {
            val language = session.getLanguage();

            String localizationKey = session.getExecutor().getLocalizationKey();
            String usageKey = localizationKey + "_usage";

            if (language.isMessageTranslated(usageKey)) {
                throw new CommandTranslationException(
                        "command_usage",
                        "%usage%", session.getExecutor().toString() + " " + language.getMessage(!session.isConsole() && ((CommonGamer) session.getSender()).isModern(), usageKey)
                );
            } else {
                throw new CommandTranslationException("command_incorrect_usage");
            }
        }
    }

    @ConditionProvider(value = "disableConsole", priority = 0)
    public static void disableConsoleCondition(ExecutionSession session, String meta)
            throws CommandTranslationException {
        if (session.isConsole()) {
            throw new CommandTranslationException("command_unavailable_console");
        }
    }

    @ConditionProvider(value = "consoleOnly", priority = 0)
    public static void consoleOnlyCondition(ExecutionSession session, String meta)
            throws CommandTranslationException {
        if (!session.isConsole()) {
            throw new CommandTranslationException("command_available_only_from_console");
        }
    }

    @ConditionProvider(value = "noAuth", priority = 0)
    public static void notAuthenticatedCondition(ExecutionSession session, String meta)
            throws CommandTranslationException {

    }

    @ConditionProvider(value = "antiDolbaeb", priority = 0)
    public static void antiDolbaeb(ExecutionSession session, String meta)
            throws CommandTranslationException {
        if (session.getSender() instanceof CommonGamer && session.getGamer().getName().equals("TheJavaCat")) {
            throw new CommandTranslationException("command_dolbaeb_protection");
        }
    }

    @RequiredArgsConstructor
    private static final class RankCondition {

        private final Group group;
        private final RankCheckMode checkMode;

        public boolean canApply(@NonNull UserRanks ranks) {
            UserRank priority = ranks.getPriorityRank(group.getPermissions());

            switch (checkMode) {
                case STRICT:
                    return priority != null && priority.matches(group);
                case EQUAL_OR_BETTER:
                    return ranks.canApply(group);
                case ONLY_BETTER:
                    return priority != null && priority.getLevel() > group.getLevel();
                case CONTAINS:
                    return ranks.contains(ranks.getRanks(group.getPermissions()), group);
            }

            return false;
        }

        public enum RankCheckMode {

/*            *
             * Only chosen group can execute*/

            STRICT,

/*            *
             * Groups higher or this can execute*/

            EQUAL_OR_BETTER,

/*            *
             * Only groups higher than selected can execute
             */
            ONLY_BETTER,

/*            *
             * Strictly if user has this group in his groups list
             */
            CONTAINS;

            public static RankCheckMode parse(final char character) {
                switch (character) {
                    case '=':
                        return STRICT;
                    case '>':
                        return ONLY_BETTER;
                    case '?':
                        return CONTAINS;
                    default:
                        return EQUAL_OR_BETTER;
                }
            }

        }

    }
}
