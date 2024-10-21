package net.skykingdoms.backend.discord.command;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.utils.AttachedFile;
import net.skykingdoms.api.protocol.util.TextFormat;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public abstract class Command implements ICommand {
    CommandData data;
    long rateLimit;

    protected Command(CommandData data) {
        this.data = data;
        rateLimit = -1;
    }

    protected Command(CommandData data, long rateLimit) {
        this.data = data;
        this.rateLimit = rateLimit;
    }

    protected void reply(@NotNull CommandExecutionSession session, String message) {
        session.originalMessage().editOriginal(message).queue();
    }

    protected void replyAsync(@NotNull SlashCommandInteractionEvent event, String message) {
        event.reply(message).queue();
    }

    protected void replyAsync(@NotNull SlashCommandInteractionEvent event, String message, Object... args) {
        replyAsync(event, TextFormat.formatText(message, args));
    }

    protected void reply(@NotNull CommandExecutionSession session, String message, Object... args) {
        reply(session, (TextFormat.formatText(message, args)));
    }

    protected void replyEmbed(@NotNull CommandExecutionSession session, MessageEmbed embed) {
        session.originalMessage().editOriginalEmbeds(embed).queue();
    }

    protected void replyEmbedEphemeral(@NotNull CommandExecutionSession session, MessageEmbed embed) {
        session.originalMessage().setEphemeral(true).editOriginalEmbeds(embed).queue();
    }

    protected void replyFile(@NotNull CommandExecutionSession session, File file) {
        session.originalMessage().editOriginalAttachments(AttachedFile.fromData(file)).queue();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Command)) return false;
        return data.getName().equals(((Command) obj).getData().getName());
    }

    public CommandData getData() {
        return data;
    }

    public long getRateLimit() {
        return rateLimit;
    }
}
