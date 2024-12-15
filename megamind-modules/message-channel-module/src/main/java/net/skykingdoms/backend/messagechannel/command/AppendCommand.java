package net.skykingdoms.backend.messagechannel.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.messagechannel.manager.MessageManager;

/**
 * @author Vihara
 */
@WithAlias("a")
@NamedCommand("append")
public final class AppendCommand {

    @Executor
    @WithCondition("minArgs: 1")
    @WithCondition("disableConsole")
    public static void execute(final CoreExecutionSession session) throws CommandTranslationException {
        val sender = session.getGamer();
        val gamer = MessageManager.INSTANCE.getLastFrom(sender);

        if (gamer == null || !gamer.isOnline() || (gamer.isVanished() && !sender.canSeeVanish())) {
            throw new CommandTranslationException("append_nobody_pm");
        }

/*        if (!gamer.getRanks().canApply(Group.HELPER) && sender.isVanished()) {
            throw new CommandTranslationException("function_disabled_vanish");
        }*/

        MessageManager.INSTANCE.processMessage(sender, gamer, session.getArgumentString());
    }
}
