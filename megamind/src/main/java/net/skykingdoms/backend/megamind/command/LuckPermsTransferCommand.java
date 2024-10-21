package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.api.protocol.util.TextFormat;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.base.permission.RankSqlHandler;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.UUID;

@NamedCommand("transferluckperms")
public class LuckPermsTransferCommand {
    private static final MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("luckperms"));
    private static final MySQL playerData = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));
    private static final MySQL sysData = new MySQL(DatabaseCredentials.MAIN.withDatabase("SysInternal"));

    @Executor
    public static void execute(CoreExecutionSession session) {
        database.executeQuery("SELECT * FROM luckperms_group_permissions;").thenAccept(rows -> {
            for (var row : rows) {
                String name = row.getRequiredString("name");
                String permission = row.getRequiredString("permission");
                boolean value = row.getBoolean("value");
                String server = row.getRequiredString("server");
                long expiry = row.getRequiredLong("expiry");

                Logging.IMP.info(TextFormat.formatText("[LuckPermsTransfer/Group] {}, {}, {}, {}", name, permission, value, server));

                if (name.startsWith("skypvp")) {
                    continue;
                }

                Group group = null;

                try {
                    group = Group.valueOf(name.toUpperCase());
                } catch (Exception e) {
                    continue;
                }

                GameMode gameMode;
                if (server.equalsIgnoreCase("global")) {
                    gameMode = null;
                } else if (server.equalsIgnoreCase("survival")) {
                    gameMode = GameMode.SURVIVAL_BEES;
                } else {
                    try {
                        gameMode = GameMode.valueOf(server.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        session.sendMessage("Unknown game mode: " + name);
                        continue;
                    }
                }

                Timestamp expiryTimestamp = (expiry == 0) ? null : new Timestamp(expiry * 1000L);

                if (gameMode == null) {
                    sysData.executeVoidUpdate("INSERT INTO GroupPermissions (GroupId, Permission, Value, GameMode, Expires) VALUES (?, ?, ?, ?, ?)",
                            group.getId(),
                            permission,
                            value,
                            "GLOBAL",
                            expiryTimestamp
                    );
                } else {
                    sysData.executeVoidUpdate("INSERT INTO GroupPermissions (GroupId, Permission, Value, GameMode, Expires) VALUES (?, ?, ?, ?, ?)",
                            group.getId(),
                            permission,
                            value,
                            gameMode.name(),
                            expiryTimestamp
                    );
                }
            }
        }).join();

        database.executeQuery("SELECT * FROM luckperms_user_permissions;").thenAccept(rows -> {
            for (var row : rows) {
                UUID uuid = UUID.fromString(row.getRequiredString("uuid"));
                String permission = row.getRequiredString("permission");
                boolean value = row.getBoolean("value");
                String server = row.getRequiredString("server");
                long expiry = row.getRequiredLong("expiry");

                Logging.IMP.info(TextFormat.formatText("[LuckPermsTransfer/User] {}, {}, {}, {}", uuid.toString(), permission, value, server));

                Timestamp expiryTimestamp = (expiry == 0) ? null : new Timestamp(expiry * 1000L);

                int id = NetworkSqlHandler.INSTANCE.getPlayerId(uuid);

 /*               if (id == -1) {
                    id = NetworkSqlHandler.INSTANCE.createUserRecord(uuid, name, Language.ENGLISH, new InetSocketAddress("127.0.0.1", 25565).getAddress());
                }*/

                if (id == -1) {
                    continue;
                }

                GameMode gameMode;
                if (server.equalsIgnoreCase("global")) {
                    gameMode = null;
                    if (permission.startsWith("group.")) {
                        Group group;

                        try {
                            group = Group.valueOf(permission.split("\\.")[1].toUpperCase());
                        } catch (Exception e) {
                            continue;
                        }

                        if (expiryTimestamp == null || expiryTimestamp.toLocalDateTime() == null) {
                            RankSqlHandler.INSTANCE.addUserRank(id, group, null);
                        } else {
                            RankSqlHandler.INSTANCE.addUserRank(id, group, expiryTimestamp.toLocalDateTime());
                        }
                        continue;
                    }
                } else if (server.equalsIgnoreCase("survival")) {
                    gameMode = GameMode.SURVIVAL_BEES;
                } else {
                    try {
                        gameMode = GameMode.valueOf(server.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        session.sendMessage("Unknown game mode: " + server);
                        continue;
                    }
                }

                if (gameMode == null) {
                    playerData.executeVoidUpdate("INSERT INTO UserPermissions (GamerId, Permission, Value, GameMode, Expires) VALUES (?, ?, ?, ?, ?)",
                            id,
                            permission,
                            value,
                            "GLOBAL",
                            expiryTimestamp
                    );
                } else {
                    playerData.executeVoidUpdate("INSERT INTO UserPermissions (GamerId, Permission, Value, GameMode, Expires) VALUES (?, ?, ?, ?, ?)",
                            id,
                            permission,
                            value,
                            gameMode.name(),
                            expiryTimestamp
                    );
                }
            }
        }).join();
    }
}
