package net.skykingdoms.backend.messagechannel.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.messagechannel.manager.MessageManager;

@WithAlias("r")
@NamedCommand("reply")
public final class ReplyCommand {

    @Executor
    @WithCondition("minArgs: 1")
    public static void execute(CoreExecutionSession session) throws CommandTranslationException {
        val sending = session.getGamer();
        val receiving = MessageManager.INSTANCE.getLastTo(sending);

        if (receiving == null || !receiving.isOnline() || receiving.isVanished() && !sending.canSeeVanish()) {
            throw new CommandTranslationException("reply_nobody_pm");
        }

/*        if (!receiving.getRanks().canApply(Group.HELPER) && sending.isVanished()) {
            throw new CommandTranslationException("function_disabled_vanish");
        }*/

        MessageManager.INSTANCE.processReply(sending, receiving, session.getArgumentString());
    }
}
