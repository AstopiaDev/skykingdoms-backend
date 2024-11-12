package net.skykingdoms.backend.megamind.data.punishment.command.impl;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.RelayExecutor;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.punishment.command.PunishmentCommand;

/**
 * Main ban command. Issues temporal or permanent bans on player
 * @author Vihara
 */
@NamedCommand("ban")
public final class BanCommand extends PunishmentCommand {

    /**
     * Permanent ban
     */
    @WithCondition("group: moderator")
    @WithCondition("minArgs: 1")
    @Executor
    public void executePermanent(final CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroups);

        executePunishment(
                target,
                session.getSender(),
                session.getArgumentCount() == 1 ? "Unspecified" : session.getArgumentString(1),
                null, // permanent
                PunishmentType.BAN);
    }

    /**
     * Temporal ban
     */
    @WithCondition("group: moderator")
    @WithCondition("minArgs: 2")
    @RelayExecutor("tempban")
    public void executeTemporal(final CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroups);
        executePunishment(
                target,
                session.getSender(),
                session.getArgumentString(2),
                parseDuration(session.getArgument(1)),
                PunishmentType.BAN);
    }

}
