package net.skykingdoms.backend.base.command.execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.api.backend.command.exception.CommandException;
import net.skykingdoms.api.backend.command.execution.IExecutionSession;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.backend.permission.UserRankContainer;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.gamer.IBaseGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.base.util.color.ChatColor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Vihara
 */
@Getter
@RequiredArgsConstructor
public abstract class ExecutionSession implements IExecutionSession {

    private final WrappedCommandExecutor executor;

    @Getter
    private final String[] arguments;

    private List<Object> providedObjects;

    public UserRankContainer<? extends IUserRank> getRanks() {
        return isConsole() ? UserRanks.CONSOLE : getGamer().getRanks();
    }

    public boolean isConsole() {
        return getGamer() == null && getSender().isConsole();
    }

    public abstract ICommandSender getSender();

    public abstract IBaseGamer getGamer();

    @Override
    public void sendMessage(@Nullable String message) {
        getSender().sendMessage(message == null ? "null" : ChatColor.GRAY + message);
    }

    public void sendPrefixedMessage(@Nullable String message) {
        sendMessage(getLanguage().getMessage(!getSender().isConsole() && ((CommonGamer) getSender()).isModern(), "prefix") + message);
    }

    @Override
    public String[] getArguments(int offset) {
        return offset == 0 ? arguments : Arrays.copyOfRange(arguments, offset, arguments.length);
    }

    @Override
    public String getArgumentString() {
        return getArgumentString(0);
    }

    @Override
    public String getArgumentString(int offset) {
        return String.join(" ", getArguments(offset));
    }

    @Override
    public int getArgumentCount() {
        return arguments.length;
    }

    @Override
    public String getArgument(int i) {
        return arguments[i];
    }

    @Override
    public int getIntArgument(int i, String field) throws CommandException {
        String argument = getArgument(i);

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            Language language = getLanguage();

            throw new CommandTranslationException("command_int_required",
                    "%arg%", language.isMessageTranslated(field) ? language.getMessage(!getSender().isConsole() && ((CommonGamer) getSender()).isModern(), field) : field);
        }
    }

    public int getProperty(String property) {
        if (isConsole()) {
            throw new IllegalStateException();
        }

        return getGamer().getProperty(property);
    }

    public void setProperty(String property, int value) {
        if (isConsole()) {
            throw new IllegalStateException();
        }

        getGamer().setProperty(property, value);
    }

    public void sendCommandUsage(String commandName) {
        sendLocalizedMessage("command_usage", "%usage%", "/stream "
                + getLanguage().getMessage(!getSender().isConsole() && ((CommonGamer) getSender()).isModern(), "command_" + commandName + "_usage"));
    }

    @Override
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
        return getLanguage().getMessage(!getSender().isConsole() && ((CommonGamer) getSender()).isModern(), key, objects);
    }

    public void sendLocalizedMessage(String key, Object... objects) {
        sendMessage(getLocalizedMessage(key, objects));
    }

    public Language getLanguage() {
        return isConsole() ? Language.DEFAULT : (Language) getGamer().getLanguage();
    }
}
