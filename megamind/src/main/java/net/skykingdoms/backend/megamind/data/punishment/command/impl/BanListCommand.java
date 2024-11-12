package net.skykingdoms.backend.megamind.data.punishment.command.impl;


import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.punishment.command.PunishmentListCommand;

/**
 * Lists all ban records
 * @author Vihara
 */
@WithAlias("bans")
@NamedCommand("banlist")
public final class BanListCommand extends PunishmentListCommand {

    @WithCondition("group: moderator")
    @WithCondition("minArgs: 1")
    @Executor
    public void execute(final CoreExecutionSession session) throws CommandException {
        sendPunishmentsList(session, PunishmentType.BAN);
    }

}
