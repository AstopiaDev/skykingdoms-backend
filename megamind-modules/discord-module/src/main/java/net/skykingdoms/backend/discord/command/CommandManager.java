package net.skykingdoms.backend.discord.command;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public enum CommandManager {
    INSTANCE;

    HashMap<String, Command> commandsList = new HashMap<>();

    public void addCommand(Command command) {
        commandsList.put(command.getData().getName(), command);
    }

    public Command getCommand(String commandName) {
        return commandsList.get(commandName);
    }

}
