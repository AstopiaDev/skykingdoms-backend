package net.skykingdoms.backend.base.command.condition;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.backend.base.command.execution.WrappedCommandExecutor;

@Getter
public class WrappedSubcommandExecutor extends WrappedCommandExecutor {

    private final String[] names;

    public WrappedSubcommandExecutor(final String @NonNull [] names,
                                     final @NonNull net.skykingdoms.backend.base.command.CommandParams params) {
        super(params);

        this.names = names;
    }

    @Override
    protected String asKey() {
        return super.asKey() + "." + names[0];
    }

    @Override
    public String asLocalizationKey() {
        return super.asLocalizationKey() + "_" + names[0];
    }

    @Override
    public String toString() {
        return super.toString() + " " + names[0];
    }
}
