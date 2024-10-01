package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.util.Patterns;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

/**
 * @author Vihara
 */
@NamedCommand("setproperty")
public final class SetPropertyCommand {

    @Executor
    @Tabcomplete("[playerName] [property]")
    @WithCondition("group: admin")
    // 
    @WithCondition("minArgs: 3")
    public static void execute(CoreExecutionSession session) throws CommandMessageException {
        Gamer target = session.getOfflineGamer(0, loadingGamer -> {
            loadingGamer.loadGroups();
            loadingGamer.loadProperties();
        });

        PropertyChangeMode selectedMode = null;

        int value = -1;

        for (PropertyChangeMode mode : PropertyChangeMode.values()) {
            try {
                value = mode.processArgument(session.getArgument(2));

                selectedMode = mode;

                break;
            } catch (IllegalArgumentException ignored) {

            }
        }

        if (selectedMode == null) {
            throw new CommandMessageException("Вы указали неверное значение");
        }

        val key = session.getArgument(1);
        if (key == null) {
            throw new CommandTranslationException("command_setproperty_invalidproperty");
        }

        selectedMode.apply(target, key, value);

        session.sendLocalizedMessage("command_setproperty_result",
                "%key%", key,
                "%player%", target.getDisplayName(),
                "%value%", session.getArgument(2)
        );
    }

    private enum PropertyChangeMode {

        ADD_PROPERTY {
            @Override
            public int processArgument(@NonNull String argument) {
                boolean isAdding = argument.startsWith("+");

                if (isAdding) {
                    argument = argument.replace("+", "");
                }

                if (isAdding && Patterns.isValidInteger(argument)) {
                    return Integer.parseInt(argument);
                }

                throw new IllegalArgumentException("Invalid argument");
            }

            @Override
            public void apply(@NonNull Gamer gamer, @NonNull String key, int value) {
                gamer.addProperty(key, value);
            }
        },
        SUBSTRACT_PROPERTY {
            @Override
            public int processArgument(@NonNull String argument) {
                boolean isSubstracting = argument.startsWith("-");

                if (isSubstracting) {
                    argument = argument.replace("-", "");
                }

                if (isSubstracting && Patterns.isValidInteger(argument)) {
                    return Integer.parseInt(argument);
                }

                throw new IllegalArgumentException("Invalid argument");
            }

            @Override
            public void apply(@NonNull Gamer gamer, @NonNull String key, int value) {
                gamer.addProperty(key, -value);
            }
        },
        CHANGE_PROPERTY {
            @Override
            public int processArgument(@NonNull String argument) {
                if (Patterns.isValidInteger(argument)) {
                    return Integer.parseInt(argument);
                }

                throw new IllegalArgumentException("Invalid argument");
            }

            @Override
            public void apply(@NonNull Gamer gamer, @NonNull String key, int value) {
                gamer.setProperty(key, value);
            }
        };

        public abstract int processArgument(@NonNull String argument) throws IllegalArgumentException;

        public abstract void apply(@NonNull Gamer gamer, @NonNull String key, int value);

    }
}
