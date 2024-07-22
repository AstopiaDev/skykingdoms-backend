/*
package net.skykingdoms.backend.megamind;

import lombok.val;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.type.PropertyType;

import java.net.InetSocketAddress;
import java.util.UUID;

public enum PointsShiHandler {
    INSTANCE;

    String GRAB_POINTS = "SELECT * FROM `playerpoints`.`playerpoints_points`;";
    Database playerPoints = new MySQL(DatabaseCredentials.MAIN.withDatabase("playerpoints"));

    public void migratePlayerPoints() {
        playerPoints.executeQuery(GRAB_POINTS).thenAccept(rows -> {
            for (val row : rows) {
                UUID uuid = UUID.fromString(row.getRequiredString("uuid"));
                int points = row.getRequiredInt("points");
                int id = NetworkSqlHandler.INSTANCE.getPlayerId(uuid);

                if (id < 0) {
                    id = NetworkSqlHandler.INSTANCE.createUserRecord(
                            uuid,
                            "Unknown",
                            Language.DEFAULT,
                            new InetSocketAddress("127.0.0.1", 1000).getAddress());
                }

                Gamer gamer = Gamer.getOfflineGamer(id, AbstractGamer::loadProperties);

                gamer.setProperty(PropertyType.PLAYER_POINTS, points);
                Logging.IMP.info("Setting player points for " + uuid.toString());
            }
        }).join();
    }
}
*/
