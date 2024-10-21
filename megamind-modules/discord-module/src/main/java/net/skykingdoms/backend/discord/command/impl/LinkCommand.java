package net.skykingdoms.backend.discord.command.impl;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.skykingdoms.backend.discord.DiscordBot;
import net.skykingdoms.backend.discord.command.Command;
import net.skykingdoms.backend.discord.command.CommandExecutionSession;
import net.skykingdoms.backend.discord.controller.PlayerController;
import net.skykingdoms.backend.discord.type.DiscordSqlHandler;

import java.awt.*;
import java.util.Random;

public class LinkCommand extends Command {
    public LinkCommand() {
        super(
                Commands.slash("link", "Link your Minecraft account to your Discord account.")
        );
    }

    @Override
    public void execute(CommandExecutionSession session) {
        session.setEphemeral(true);

        DiscordSqlHandler.INSTANCE.isLinked(session.user().getId()).thenAccept(linked -> {
            if (linked) {
                EmbedBuilder embedBuilder = new EmbedBuilder();

                embedBuilder.setTitle("You are already linked!");
                embedBuilder.setDescription("You are already linked to a Minecraft account.");
                embedBuilder.setColor(Color.RED);

                replyEmbed(session, embedBuilder.build());
                return;
            }

            String randomCode = generateRandomCode();
            // You can save or use the randomCode here
            EmbedBuilder embedBuilder = new EmbedBuilder();

            embedBuilder.setTitle("Your code is:");
            embedBuilder.setDescription("`" + randomCode + "`");
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setFooter("Type /link " + randomCode + " in-game..");

            PlayerController.INSTANCE.linkCode(session.user().getId(), randomCode);

            replyEmbed(session, embedBuilder.build());
        });
    }

    private String generateRandomCode() {
        int length = 6; // You can adjust the length of the code as needed
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = DiscordBot.INSTANCE.random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(characters.charAt(random.nextInt(characters.length())));
        }

        return code.toString();
    }
}
