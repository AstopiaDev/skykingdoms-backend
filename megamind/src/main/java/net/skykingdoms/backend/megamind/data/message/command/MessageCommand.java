package net.skykingdoms.backend.megamind.data.message.command;

import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.message.MessageManager;

@WithAlias("w")
@WithAlias("whisper")
@WithAlias("msg")
@WithAlias("m")
@WithAlias("tell")
@WithAlias("text")
@WithAlias("t")
@WithAlias("pm")
@NamedCommand("message")
public final class MessageCommand {

    @Executor
    @Tabcomplete("[playerName]")
    
    @WithCondition("minArgs: 2")
    public static void execute(CoreExecutionSession session) throws CommandTranslationException {
        val sender = session.getGamer();
        val receiver = session.getGamer(0);
/*        if (!receiver.getRanks().canApply(Group.HELPER) && sender.isVanished()) {
            throw new CommandTranslationException("function_disabled_vanish");
        }*/

        if (receiver == sender) {
            throw new CommandTranslationException("message_self");
        }

        MessageManager.INSTANCE.processMessage(sender, receiver, session.getArgumentString(1));
    }

}
