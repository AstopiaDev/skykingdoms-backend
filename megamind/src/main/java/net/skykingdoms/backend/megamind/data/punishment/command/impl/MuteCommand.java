package net.skykingdoms.backend.megamind.data.punishment.command.impl;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.punishment.command.PunishmentCommand;

/**
 * Main mute command
 * @author Vihara
 */
@NamedCommand("mute")
public final class MuteCommand extends PunishmentCommand {

    @WithCondition("group: moderator")
    @WithCondition("minArgs: 2")
    @Executor
    public void execute(final CoreExecutionSession session) throws CommandTranslationException {
        val target = session.getOfflineGamer(0, CommonGamer::loadGroups);

        executePunishment(
                target,
                session.getSender(),
                session.getArgumentString(2),
                parseDuration(session.getArgument(1)),
                PunishmentType.MUTE);
    }

}
