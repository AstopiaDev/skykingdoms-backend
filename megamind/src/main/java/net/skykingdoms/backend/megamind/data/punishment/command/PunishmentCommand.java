package net.skykingdoms.backend.megamind.data.punishment.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.punishment.PunishmentType;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.megamind.api.command.CommandSender;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.data.punishment.PunishmentManager;
import org.jetbrains.annotations.Nullable;

/**
 * Basic punishment abstract command class.
 *
 * @author Vihara
 */
public abstract class PunishmentCommand {

    protected @NonNull java.time.Duration parseDuration(final @NonNull String input) throws CommandTranslationException {
        // Extract the numeric part and the unit part from the input string
        int numericValue = Integer.parseInt(input.substring(0, input.length() - 1));
        char unit = Character.toLowerCase(input.charAt(input.length() - 1));

        // Determine the duration unit based on the extracted character
        switch (unit) {
            case 'd':
                return java.time.Duration.ofDays(numericValue);
            case 'h':
                return java.time.Duration.ofHours(numericValue);
            case 'm':
                return java.time.Duration.ofMinutes(numericValue);
            default:
                throw new CommandTranslationException("punishment_invalid_duration");
        }
    }

    protected void executePunishment(final @NonNull Gamer target,
                                     final CommandSender executor,
                                     final @NonNull String reason,
                                     final @Nullable java.time.Duration duration,
                                     final @NonNull PunishmentType punishmentType) throws CommandTranslationException {

        // so mods won't ban admins :hehe:
        if (!executor.getRanks().canApply(target.getRanks().getPriorityRank().getGroup())) {
            throw new CommandTranslationException("punishment_forbidden");
        }

        val _active = PunishmentManager.INSTANCE.getActivePunishment(target.getGamerId(), punishmentType);
        if (_active != null) {
            executor.sendLocalizedMessage(!executor.isConsole() && ((CommonGamer) executor).isModern(), "punishment_already_active",
                    "%id%", _active.getMetaId());
            return;
        }

        if (punishmentType == PunishmentType.MUTE) {
            executor.sendLocalizedMessage(!executor.isConsole() && ((CommonGamer) executor).isModern(),"punishment_mute_executed",
                    "%player%", target.getDisplayName(),
                    "%duration%", Duration.durationWordsMs((Language) executor.getLanguage(), duration.toMillis()),
                    "%reason%", reason);
        }

        val punishment = PunishmentManager.INSTANCE.performPunishment(
                target.getGamerId(),
                executor.isConsole() ? 0 : ((Gamer) executor).getGamerId(),
                reason, duration, target.getCurrentServer().getName(), punishmentType);

        if (punishmentType == PunishmentType.BAN) {
            //noinspection DataFlowIssue
            if (punishment.isPermanent()) {
                executor.sendLocalizedMessage(!executor.isConsole() && ((CommonGamer) executor).isModern(), "punishment_permaban_executed",
                        "%player%", target.getDisplayName(),
                        "%reason%", reason);
            } else {
                executor.sendLocalizedMessage(!executor.isConsole() && ((CommonGamer) executor).isModern(), "punishment_tempban_executed",
                        "%player%", target.getDisplayName(),
                        "%duration%", Duration.durationWordsMs((Language) executor.getLanguage(), duration.toMillis()),
                        "%reason%", reason);
            }
        }
    }

}
