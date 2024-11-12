package net.skykingdoms.backend.megamind.data.punishment.command.impl;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.punishment.PunishmentMeta;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentManager;

@NamedCommand("unban")
public class UnbanCommand {
    @Executor
    @WithCondition("group: moderator")
    @WithCondition("minArgs: 1")
    public static void execute(CoreExecutionSession session) {
        String playerName = session.getArgument(0);
        String reason = session.getArgumentCount() > 1 ? session.getArgumentString(1) : "No reason specified";
        int id = NetworkSqlHandler.INSTANCE.getPlayerId(playerName);

        if (id == -1) {
            session.sendMessage("§cPlayer not found: " + playerName);
        }

        PunishmentMeta meta = PunishmentManager.INSTANCE.getActivePunishment(id, PunishmentType.BAN);

        if (meta == null) {
            session.sendLocalizedMessage("punishment_unban_not_banned");
            return;
        }

        int metaId = meta.getMetaId();

        PunishmentManager.INSTANCE.removePunishment(
                metaId,
                session.isConsole() ? -1 : session.getGamer().getGamerId(),
                reason
        );
        session.sendLocalizedMessage("punishment_unban_successful", "%player%", playerName, "%reason%", reason);
    }
}
