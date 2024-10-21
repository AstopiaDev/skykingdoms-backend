package net.skykingdoms.backend.discord.command;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Accessors(fluent = true)
@Getter
public final class CommandExecutionSession {
    final SlashCommandInteraction interaction;
    final InteractionHook originalMessage;
    final Member user;
    boolean isEphemeral = false;

    public CommandExecutionSession(SlashCommandInteraction interaction, InteractionHook originalMessage, Member user) {
        this.interaction = interaction;
        this.originalMessage = originalMessage;
        this.user = user;
    }

    public CommandExecutionSession(SlashCommandInteraction interaction, InteractionHook originalMessage, Member user, boolean isEphemeral) {
        this.interaction = interaction;
        this.originalMessage = originalMessage;
        this.user = user;
        this.isEphemeral = isEphemeral;
    }

    public boolean isEphemeral() {
        return isEphemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        isEphemeral = ephemeral;
    }
}
