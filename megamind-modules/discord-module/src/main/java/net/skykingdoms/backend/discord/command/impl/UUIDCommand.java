package net.skykingdoms.backend.discord.command.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.skykingdoms.backend.discord.command.Command;
import net.skykingdoms.backend.discord.command.CommandExecutionSession;

import java.awt.*;
import java.util.Date;
import java.util.UUID;

public class UUIDCommand extends Command {
    public UUIDCommand() {
        super(
                Commands.slash("uuid", "Get a player's UUID.")
                .addOption(OptionType.STRING, "player", "The player's in-game name.")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS))
        );
    }

    @Override
    public void execute(CommandExecutionSession session) {
        String playerName = session.interaction().getOption("player").getAsString();

        try {
            UUID uuid = UUID.randomUUID();
            EmbedBuilder embedBuilder = new EmbedBuilder();

            embedBuilder.setTitle(playerName);
            embedBuilder.setDescription("`" + uuid.toString() + "`");
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setFooter(new Date().toGMTString());
            replyEmbed(session, embedBuilder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
