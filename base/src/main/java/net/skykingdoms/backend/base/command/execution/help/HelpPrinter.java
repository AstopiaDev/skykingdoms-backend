package net.skykingdoms.backend.base.command.execution.help;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skykingdoms.backend.base.command.CommandWrapper;
import net.skykingdoms.backend.base.command.condition.WrappedCompiledCommandCondition;
import net.skykingdoms.backend.base.command.condition.WrappedSubcommandExecutor;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.language.Language;

import java.util.Map;

@UtilityClass
public class HelpPrinter {

    public void print(final @NonNull CommandWrapper wrapper,
                      final @NonNull ExecutionSession session) {
        StringBuilder builder = new StringBuilder();
        builder.append(session.getLanguage().getMessage(!session.isConsole() && ((CommonGamer) session.getSender()).isModern(), "command_help"));

        WrappedCommandExecutor fallback = wrapper.hasFallback() ? wrapper.getFallback() : wrapper.getExecutor();
        boolean doPrintFallback = fallback != null;

        for (Map.Entry<String, WrappedSubcommandExecutor> entry : wrapper.getSubcommands().entrySet()) {
            String name = entry.getKey();
            WrappedSubcommandExecutor executor = entry.getValue();

            if (name.equals(executor.getNames()[0].toLowerCase())) {
                print(executor, session, builder);
            }

            if (doPrintFallback && executor.getMethodHash() == fallback.getMethodHash()) {
                doPrintFallback = false;
            }
        }

        if (doPrintFallback) {
            print(fallback, session, builder);
        }

        session.sendMessage(builder.toString());
    }

    public void print(WrappedCommandExecutor executor, ExecutionSession session, StringBuilder output) {
        if (executor.isWithoutHelp()) {
            return;
        }

        for (WrappedCompiledCommandCondition condition : executor.getConditions()) {
            if (!condition.isShowInHelp()) {
                try {
                    condition.getChecker().execute(session);
                } catch (Exception e) {
                    return;
                }
            }
        }

        Language language = session.getLanguage();

        String localizationKey = executor.getLocalizationKey();

        String usageKey = localizationKey + "_usage";
        String descriptionKey = localizationKey + "_description";

        String description = language.isMessageTranslated(descriptionKey)
                ? language.getMessage(!session.isConsole() && ((CommonGamer) session.getSender()).isModern(), descriptionKey)
                : null;

        String usage = executor.toString();

        if (language.isMessageTranslated(usageKey)) {
            usage += " " + language.getMessage(!session.isConsole() && ((CommonGamer) session.getSender()).isModern(), usageKey);
        }

        String message = language.getMessage(!session.isConsole() && ((CommonGamer) session.getSender()).isModern(), "command_help_row", "%usage%", usage, "%description%", description == null
                ? language.getMessage(!session.isConsole() && ((CommonGamer) session.getSender()).isModern(), "command_help_no_description")
                : description);

        output.append("\n").append(message);
    }

}
