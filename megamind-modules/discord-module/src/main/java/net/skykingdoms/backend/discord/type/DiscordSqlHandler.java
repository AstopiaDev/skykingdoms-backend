package net.skykingdoms.backend.discord.type;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@Getter
public enum DiscordSqlHandler {
    INSTANCE;

    String LOAD_DISCORD_ID = "SELECT * FROM `LongProperties` WHERE `Property` = 'discord_id' AND `Value` = ?";

    Database database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    public CompletableFuture<Boolean> isLinked(String discordId) {
        return database.executeQuery(LOAD_DISCORD_ID, Long.valueOf(discordId))
                .thenApply(rows -> !rows.isEmpty());
    }
}
