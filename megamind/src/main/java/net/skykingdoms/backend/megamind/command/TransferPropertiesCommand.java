package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

@NamedCommand("transferprops")
public class TransferPropertiesCommand {
    private static final MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    @Executor
    @WithCondition("group: owner")
    public static void execute(CoreExecutionSession session) {
        database.executeQuery("SELECT * FROM `Properties`;").thenAccept(rows -> {
            // Iterate through the result set and insert each row into the new table
            rows.forEach(row -> {
                try {
                    int gamerId = row.getRequiredInt("Id");
                    int playerPoints = row.getRequiredInt("PLAYER_POINTS");
                    int tagId = row.getRequiredInt("TAG_ID");
                    long discordId = row.getRequiredLong("DISCORD_ID");

                    database.executeVoidUpdate(
                            "INSERT INTO `IntProperties` (GamerId, Property, Value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Value = VALUES(Value);",
                            gamerId, "player_points", playerPoints).join();

                    database.executeVoidUpdate(
                            "INSERT INTO `IntProperties` (GamerId, Property, Value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Value = VALUES(Value);",
                            gamerId, "tag_id", tagId).join();

                    database.executeVoidUpdate(
                            "INSERT INTO `LongProperties` (GamerId, Property, Value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE Value = VALUES(Value);",
                            gamerId, "discord_id", discordId).join();

                } catch (Exception e) {
                    session.sendMessage("Error processing row for gamerId: " + e.getMessage());
                }
            });
        }).exceptionally(throwable -> {
            session.sendMessage("Error querying database: " + throwable.getMessage());
            return null;
        }).join();

        session.sendMessage("Done.");
    }
}