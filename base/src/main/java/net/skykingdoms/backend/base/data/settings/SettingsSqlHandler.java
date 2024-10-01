package net.skykingdoms.backend.base.data.settings;

import lombok.val;
import net.skykingdoms.api.backend.setting.Setting;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.impl.BooleanSetting;
import net.skykingdoms.api.backend.setting.impl.EnumSetting;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.EnumMap;
import java.util.Map;

public enum SettingsSqlHandler {
    INSTANCE;

    private static final String LOAD_SETTINGS = "SELECT * FROM `PlayerData`.`UserSettings` WHERE `Id` = ?;";
    private static final String UPDATE_SETTINGS = "INSERT INTO `PlayerData`.`Settings` " +
            "(`Id`, `PLAYERS_VISIBILITY`, `PET_VISIBILITY`, `AUTO_SPAWN_PET`, `LOBBY_DARK_MODE`, `API_ONLINE_STATUS`, `API_RECENT_GAMES`, `API_WINSTREAKS`, `GUILD_NOTIFICATIONS`, `GUILD_SHOW_ONLINE_ONLY`, `PARTICLE_QUALITY`, `CHAT_VISIBILITY`, `GUILD_CHAT_VISIBILITY`, `LOBBY_JOIN_MESSAGE`, `PUBLIC_CHAT_FILTER`, `PARTY_CHAT_FILTER`, `GUILD_CHAT_FILTER`, `PM_CHAT_FILTER`, `PARTY_MESSAGE_SOUNDS`, `GUILD_MESSAGE_SOUNDS`, `PM_SOUNDS`, `CHAT_ALERTS`, `ONLINE_STATUS`, `PM_PRIVACY`, `FRIEND_REQUEST`, `FRIEND_NOTIFICATIONS`, `DUEL_INVITE_PRIVACY`, `PARTY_INVITE_PRIVACY`, `GUILD_INVITE_PRIVACY`, `LEGACY`, `VANISH_ON_JOIN`, `VANISH_ITEM_PICKUP`) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE" +
            "? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?, ? = ?;";

    private final MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("PlayerData"));

    public EnumMap<SettingType, Setting<?, ?>> loadSettings(final int gamerId) {
        return database.executeQuery(LOAD_SETTINGS, gamerId).thenApply(rows -> {
            val ret = new EnumMap<SettingType, Setting<?, ?>>(SettingType.class);

            if (rows.isEmpty()) {
                for (val st : SettingType.VALUES) {
                    ret.put(st, st.getSetting());
                }
                return ret;
            }

            val row = rows.getFirst();
            for (val settingType : SettingType.VALUES) {
                if (settingType.getSetting() instanceof BooleanSetting) {
                    val bs = (BooleanSetting) settingType.getSetting();
                    ret.put(settingType, bs.deserialize(row.getBoolean(settingType.name())));
                }
                if (settingType.getSetting() instanceof EnumSetting) {
                    val es = (EnumSetting) settingType.getSetting();
                    es.deserialize(row.getRequiredInt(settingType.name()));
                    ret.put(settingType, es);
                }
            }

            return ret;
        }).join();
    }

    public void updateSettings(final int gamerId, Map<SettingType, Setting<?, ?>> settings) {
        val sql = new StringBuilder("INSERT INTO `PlayerData`.`UserSettings` (`Id`");
        for (val st : SettingType.VALUES) {
            sql.append(", `" + st.name() + "`");
        }

        sql.append(") VALUES(" + gamerId);

        for (val s : SettingType.VALUES) {
            val value = settings.get(s);
            sql.append(", " + value.serialize());
        }

        sql.append(") ON DUPLICATE KEY UPDATE ");

        for (val s : SettingType.VALUES) {
            val value = settings.get(s);
            sql.append("`" + s.name() + "` = " + value.serialize() + (s.ordinal() == SettingType.VALUES.length - 1 ? "" : ", "));
        }

        sql.append(";");

        database.executeUpdate(sql.toString());
    }
}
