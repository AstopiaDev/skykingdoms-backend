package net.skykingdoms.backend.base.command.execution.help;

import lombok.NonNull;
import net.skykingdoms.backend.base.command.CommandParams;
import net.skykingdoms.backend.base.command.CommandWrapper;
import net.skykingdoms.backend.base.command.condition.WrappedSubcommandExecutor;

public class HelpSubcommandExecutor extends WrappedSubcommandExecutor {

    private static final String[] NAMES = new String[]{"help"};
    private static final int HASH = "help".hashCode();

    public HelpSubcommandExecutor(final @NonNull CommandWrapper wrapper) {
        super(
                NAMES, new CommandParams(wrapper, HASH, session -> HelpPrinter.print(wrapper, session))
        );
    }

}
