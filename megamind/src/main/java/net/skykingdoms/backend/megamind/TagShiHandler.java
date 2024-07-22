/*
package net.skykingdoms.backend.megamind;

import lombok.val;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.tags.TagsSqlHandler;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.logging.Logging;

import java.util.UUID;

public enum TagShiHandler {
    INSTANCE;

    String GRAB_LUCK_TAGS = "SELECT * FROM `luckperms`.`luckperms_user_permissions`;";
    Database luckPerms = new MySQL(DatabaseCredentials.MAIN.withDatabase("luckperms"));

    public void startMigrationShit() {
        luckPerms.executeQuery(GRAB_LUCK_TAGS).thenAccept(rows -> {
            for (val row : rows) {
                UUID uuid = UUID.fromString(row.getRequiredString("uuid"));
                String permission = row.getRequiredString("permission");
                if (permission.startsWith("tags.")) {
                    Logging.IMP.info("Adding tag for " + uuid.toString());
                    String[] split = permission.split("\\.");
                    String name = split[1];
                    luckPerms.executeQuery("SELECT * FROM `SysInternal`.`Tags` WHERE `NameId` = ?", name).thenAccept(rows1 -> {
                        if (rows1.isEmpty()) {
                            return;
                        }

                        val tag = rows1.getFirst();
                        TagsSqlHandler.INSTANCE.addPlayerTag(uuid, tag.getRequiredInt("Id"));
                    }).join();
                }
            }
        }).join();
    }
}
*/
