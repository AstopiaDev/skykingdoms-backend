package net.skykingdoms.backend.base.command.execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.backend.base.command.exception.CommandException;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.ICommandSender;
import net.skykingdoms.backend.base.gamer.IGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.protocol.type.PropertyType;
import net.skykingdoms.backend.protocol.util.TextFormat;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vihara
 */
@Getter
@RequiredArgsConstructor
public abstract class ExecutionSession {

    private final WrappedCommandExecutor executor;

    @Getter
    private final String[] arguments;

    private List<Object> providedObjects;

    public UserRanks getRanks() {
        return isConsole() ? UserRanks.CONSOLE : getGamer().getRanks();
    }

    public boolean isConsole() {
        return getGamer() == null && getSender().isConsole();
    }

    public abstract ICommandSender getSender();

    public abstract IGamer getGamer();

    public void sendMessage(@Nullable String message) {
        getSender().sendMessage(message == null ? "null" : ChatColor.GRAY + message);
    }

    public void sendPrefixedMessage(@Nullable String message) {
        sendMessage(getLanguage().getMessage(!getSender().isConsole() && ((AbstractGamer) getSender()).isModern(), "prefix") + message);
    }

    public String[] getArguments(int offset) {
        return offset == 0 ? arguments : Arrays.copyOfRange(arguments, offset, arguments.length);
    }

    public String getArgumentString() {
        return getArgumentString(0);
    }

    public String getArgumentString(int offset) {
        return String.join(" ", getArguments(offset));
    }

    public int getArgumentCount() {
        return arguments.length;
    }

    public String getArgument(int i) {
        return arguments[i];
    }

    public int getIntArgument(int i, String field) throws CommandException {
        String argument = getArgument(i);

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            Language language = getLanguage();

            throw new CommandTranslationException("command_int_required",
                    "%arg%", language.isMessageTranslated(field) ? language.getMessage(!getSender().isConsole() && ((AbstractGamer) getSender()).isModern(), field) : field);
        }
    }

    public int getProperty(PropertyType property) {
        if (isConsole()) {
            throw new IllegalStateException();
        }

        return getGamer().getProperty(property);
    }

    public void setProperty(PropertyType property, int value) {
        if (isConsole()) {
            throw new IllegalStateException();
        }

        getGamer().setProperty(property, value);
    }

    public void sendCommandUsage(String commandName) {
        sendLocalizedMessage("command_usage", "%usage%", "/stream "
                + getLanguage().getMessage(!getSender().isConsole() && ((AbstractGamer) getSender()).isModern(), "command_" + commandName + "_usage"));
    }

    public void sendMessage(@Nullable String message, @Nullable Object... o) {
        if (message == null) {
            sendMessage(null);

            return;
        }

        if (o == null) {
            sendMessage(message);

            return;
        }

        // We send gray-colored message by default, because white is ugly :(
        sendMessage(TextFormat.formatText(message, o));
    }

    @SuppressWarnings("unchecked")
    public <T> T getProvidedObject(int index) {
        return providedObjects == null ? null : (T) providedObjects.get(index);
    }

    public void provideObject(int index, Object object) {
        if (providedObjects == null) {
            providedObjects = new ArrayList<>(3);
        }

        if (index == providedObjects.size()) {
            providedObjects.add(object);
        }

        for (int i = providedObjects.size(); i <= index + 1; i++) {
            providedObjects.add(null);
        }

        providedObjects.set(index, object);
    }

    public String getDisplayName() {
        return isConsole() ? "§4Console" : getGamer().getDisplayName();
    }

    public abstract void sync(Runnable runnable);

    public String getLocalizedMessage(String key, Object... objects) {
        return getLanguage().getMessage(!getSender().isConsole() && ((AbstractGamer) getSender()).isModern(), key, objects);
    }

    public void sendLocalizedMessage(String key, Object... objects) {
        sendMessage(getLocalizedMessage(key, objects));
    }

    public Language getLanguage() {
        return isConsole() ? Language.DEFAULT : getGamer().getLanguage();
    }
}
