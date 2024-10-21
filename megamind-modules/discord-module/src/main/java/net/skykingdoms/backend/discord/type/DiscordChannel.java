package net.skykingdoms.backend.discord.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.skykingdoms.backend.discord.DiscordBot;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public enum DiscordChannel {
    REPORTS("1218764907390963803")
    ;

    String id;
    public TextChannel channel() {
        return DiscordBot.INSTANCE.guild().getTextChannelById(id);
    }
}
