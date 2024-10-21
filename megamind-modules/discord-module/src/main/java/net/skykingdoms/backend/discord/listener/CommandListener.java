package net.skykingdoms.backend.discord.listener;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.skykingdoms.backend.discord.DiscordBot;
import net.skykingdoms.backend.discord.command.CommandExecutionSession;
import net.skykingdoms.backend.discord.controller.CommandController;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        registerCommands(event);
        Guild guild = event.getGuild();
        if (guild.getId().equalsIgnoreCase(DiscordBot.INSTANCE.guildId())) {
            DiscordBot.INSTANCE.guild(guild);
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        registerCommands(event);
        Guild guild = event.getGuild();
        if (guild.getId().equalsIgnoreCase(DiscordBot.INSTANCE.guildId())) {
            DiscordBot.INSTANCE.guild(guild);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member == null) return;

        CompletableFuture<InteractionHook> future = new CompletableFuture<InteractionHook>();
        event.deferReply().setEphemeral(true).queue(future::complete);

        final InteractionHook hook = future.join();
        final CommandExecutionSession session = new CommandExecutionSession(event.getInteraction(), hook, member);

        CommandController.INSTANCE.getCommand(event.getName()).execute(session);
    }

    private void registerCommands(final GenericGuildEvent event) {
        CommandController.INSTANCE.getCommands().values().forEach(command -> event.getGuild().updateCommands().addCommands(command.getData()).queue());
    }
}
