package net.skykingdoms.backend.discord.controller;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Guild;
import net.skykingdoms.backend.discord.DiscordBot;
import net.skykingdoms.backend.discord.command.Command;
import net.skykingdoms.backend.discord.command.impl.LinkCommand;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public enum CommandController {
    INSTANCE;

    Map<String, Command> commands = new HashMap<>();

    public void init() {
        this.addCommand(new LinkCommand());
        Guild guild = DiscordBot.INSTANCE.guild();
        commands.values().forEach(command -> guild.updateCommands().addCommands(command.getData()).queue());
        System.out.println("CommandController is ready!");
    }

    public void addCommand(Command command) {
        commands.put(command.getData().getName(), command);
    }

    public Command getCommand(String commandName) {
        return commands.get(commandName);
    }

    public Map<String, Command> getCommands() {
        return commands;
    }
}
