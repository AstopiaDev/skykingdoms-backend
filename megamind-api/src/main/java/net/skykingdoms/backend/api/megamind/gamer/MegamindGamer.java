package net.skykingdoms.backend.api.megamind.gamer;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.data.property.PropertySqlHandler;
import net.skykingdoms.backend.base.data.settings.Setting;
import net.skykingdoms.backend.base.data.settings.SettingType;
import net.skykingdoms.backend.base.data.settings.SettingsSqlHandler;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.IGamer;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MegamindGamer extends AbstractGamer implements IGamer {

    /**
     * Which hostname user used to connect to le server
     */
    @Setter
    String domain;

    /**
     * Login date
     */
    @Getter @Setter
    LocalDateTime firstLogin;
    @Getter @Setter
    LocalDateTime lastLogin;

    /**
     * If the player is online
     */
    @Getter @Setter boolean online;

    public MegamindGamer(final @NonNull UUID playerUniqueId) {
        super(playerUniqueId);
    }

    public MegamindGamer(final @NonNull String username) {
        super(username);
    }

    // region settings

    @Override
    public void setSetting(final @NonNull SettingType settingType,
                           final @NonNull Setting<?, ?> value) {

        updateSetting(settingType, value);
        sendSettingChange(settingType, value);
    }

    public void updateSetting(final @NonNull SettingType settingType,
                              final @NonNull Setting<?, ?> value) {
        if (gamerId < 0 || settings == null || settings.isEmpty()) {
            return;
        }

        Logging.IMP.info("REPLACE CALLED {} -> {}", settingType, value);
        settings.replace(settingType, value);

        SettingsSqlHandler.INSTANCE.updateSettings(gamerId,
                settings);
    }

    private void sendSettingChange(final @NonNull SettingType settingType,
                                   final @NonNull Setting<?, ?> value) {
        val packet = new B109UpdateSetting(gamerId, settingType.name(), String.valueOf(value.serialize()));
        sendToServer(packet);
        sendToProxy(packet);
    }

    // endregion

    // region properties

    @Override
    public void addProperty(@NonNull PropertyType property, int value) {
        int oldValue = getProperty(property);
        int newValue = oldValue + value;

        updateProperty(property, newValue);
        sendPropertyChange(property, newValue);
    }

    @Override
    public void setProperty(final @NonNull PropertyType property,
                            final int value) {
        updateProperty(property, value);
        sendPropertyChange(property, value);
    }

    private void sendPropertyChange(final @NonNull PropertyType property,
                                    final int value) {
        Packet<?> packet = new B102SetProperty(gamerId, property, value);
        sendToServer(packet);
        sendToProxy(packet);
    }

    public void updateProperty(final @NonNull PropertyType key,
                               final int value) {
        if (gamerId < 0 || properties == null) {
            return;
        }

        properties.put(key, value);

        PropertySqlHandler.INSTANCE.setPropertyValue(gamerId, key, value);
    }

    @Override
    public void removeProperty(final @NonNull PropertyType key) {
        if (gamerId < 0 || properties == null) {
            return;
        }

        dropProperty(key);

        B103RemoveProperty packet = new B103RemoveProperty(gamerId, key);
        sendToProxy(packet);
        sendToServer(packet);
    }

    @Override
    public void dropProperty(final @NonNull PropertyType key) {
        if (gamerId < 0 || properties == null) {
            return;
        }

        properties.remove(key);

        PropertySqlHandler.INSTANCE.removeProperty(gamerId, key);
    }

    @Override
    public void load() {
        super.load();
        // todo: add more ...
    }

    @Override
    public void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut) {
        sendToProxy(new S13SendTitle(gamerId, title, subtitle, fadeIn, stay, fadeOut));
    }

    @Override
    public void disconnect(@NonNull String reason) {
        sendToProxy(new B107ProxyKick(gamerId, reason));
    }


    @Override
    public void sendRawMessage(String message) {
        sendToProxy(new B106Chat(gamerId, message, true));
    }

    @Override
    public void sendMessage(@NonNull String message) {
        sendToProxy(new B106Chat(gamerId, message, false));
    }

    public void sendLocalizedMessage(@NonNull String key, Object... objects) {
        super.sendLocalizedMessage(isModern(), key, objects);
    }

    // region data loaders

    // relay
    public static MegamindGamer getGamer(final @NonNull String name) {
        return AbstractGamer.getGamer(name);
    }

    // relay
    public static MegamindGamer getOfflineGamer(final @NonNull String name) {
        return AbstractGamer.getOfflineGamer(name);
    }

    // relay
    public static MegamindGamer getOfflineGamer(final @NonNull UUID uuid) {
        return AbstractGamer.getOfflineGamer(uuid);
    }

    // relay
    public static MegamindGamer getOfflineGamer(final int playerId) {
        return AbstractGamer.getOfflineGamer(playerId);
    }

    // relay
    public static MegamindGamer getGamer(final int playerId) {
        return AbstractGamer.getGamer(playerId);
    }

    // relay
    public static void kickAll() {
        AbstractGamer.kickAll();
    }


    public static List<IGamer> getGamersByIp(final @NonNull InetAddress address) {
        synchronized (GAMERS) {
            return GAMERS.values().stream()
                    .filter(player -> ((AbstractGamer) player).getAddress().equals(address))
                    .collect(Collectors.toList());
        }
    }
/*
    public static List<IGamer> getGamersByGroup(final @NonNull Group group) {
        synchronized (GAMERS) {
            return GAMERS.values().stream()
                    .filter(player -> player.getRanks().canApply(group))
                    .collect(Collectors.toList());
        }
    }*/

    // relay
    public static void invalidateOfflinePlayer(final @NonNull MegamindGamer gamer) {
        AbstractGamer.invalidateOfflinePlayer(gamer);
    }

    // relay
    public static int getTotalOnline() {
        return AbstractGamer.getTotalOnline();
    }
    // endregion
    @Override
    public MegamindGamer serialize(@NonNull ByteBuf out) {
        super.serialize(out);

        return this;
    }

    @Override
    public MegamindGamer deserialize(@NonNull ByteBuf in) {
        super.deserialize(in);
        return this;
    }
}