package net.skykingdoms.minimind.social;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.setting.Setting;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.permission.UserRank;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.minimind.command.CommandSender;
import net.skykingdoms.minimind.social.party.Party;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

public final class SocialGamer extends CommonGamer implements CommandSender, IMegamindGamer {
    @Getter @Setter
    String currentServer;

    @Setter @Getter
    Party party;

    public SocialGamer(@NonNull UUID uuid) {
        super(uuid);
    }

    public SocialGamer(@NonNull String name) {
        super(name);
    }

    @Override
    public void setSetting(@NonNull SettingType settingType, @NonNull Setting<?, ?> value) {

    }

    @Override
    public <T> void setProperty(@NonNull String property, T value) {

    }

    @Override
    public <T> void updateProperty(@NonNull String key, T value) {

    }

    @Override
    public void removeProperty(@NonNull String key) {

    }

    @Override
    public void dropProperty(@NonNull String key) {

    }

    @Override
    public void addProperty(@NonNull String property, int value) {

    }

    @Override
    public void sendTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void disconnect(@NotNull String reason) {

    }

    @Override
    public void addRank(@NonNull Group group, LocalDateTime expires) {

    }

    @Override
    public void addRank(@NonNull UserRank rank) {

    }

    @Override
    public void setRank(@NonNull Group group, LocalDateTime expires) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void sendLocalizedMessage(boolean modern, @NonNull String key, Object... objects) {
        super.sendLocalizedMessage(modern, key, objects);
    }

    @Override
    public void dispatchCommand(String command) {

    }

    // non-relay
    public static SocialGamer getOfflineGamer(final @NonNull String name,
                                        final @Nullable Consumer<SocialGamer> consumer) {
        val onlineGamer = (SocialGamer) getGamer(name);

        if (onlineGamer != null) {
            return onlineGamer;
        }

        var offlineGamer = (SocialGamer) getOfflineGamer(name);

        boolean isNull = offlineGamer == null;

        if (isNull) {
            offlineGamer = new SocialGamer(name);
            offlineGamer.loadPlayerId();

            if (offlineGamer.getGamerId() < 0) {
                return null;
            }

            // loading name in normal case
            offlineGamer.loadPlayerName();

            synchronized (OFFLINE_GAMERS) {
                OFFLINE_GAMERS.store(offlineGamer);
            }
        }

        // invoking loader
        if (consumer != null) consumer.accept(offlineGamer);

        if (isNull) {
/*
            MegamindApp.INSTANCE.pluginManager().callEvent(new OfflineGamerLoadedEvent(offlineGamer));
*/
        }

        return offlineGamer;
    }

    // non-relay
    public static SocialGamer getOfflineGamer(final int playerId,
                                        final @NonNull Consumer<SocialGamer> loader) {
        val onlineGamer = (SocialGamer) getGamer(playerId);

        if (onlineGamer != null) {
            return onlineGamer;
        }

        val uuid = NetworkSqlHandler.INSTANCE.getUniqueId(playerId);

        if (uuid == null) {
            return null;
        }

        val offlineGamer = new SocialGamer(uuid);
        offlineGamer.setGamerId(playerId);

        // loading name in normal case
        offlineGamer.loadPlayerName();

        synchronized (OFFLINE_GAMERS) {
            OFFLINE_GAMERS.store(offlineGamer);
        }

        loader.accept(offlineGamer);

        return offlineGamer;
    }

    public static SocialGamer getGamer(final int playerId) {
        return CommonGamer.getGamer(playerId);
    }

    @Override
    public SocialGamer serialize(@NonNull ByteBuf out) {
        out.writeBoolean(isOnline());
        PacketUtils.writeString(out, currentServer);
        return this;
    }

    @Override
    public SocialGamer deserialize(@NonNull ByteBuf in) {
        return null;
    }

    @Override
    public String getDomain() {
        return "";
    }

    @Override
    public LocalDateTime getFirstLogin() {
        return null;
    }

    @Override
    public LocalDateTime getLastLogin() {
        return null;
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public void updateSetting(@NonNull SettingType settingType, @NonNull Setting<?, ?> value) {

    }

    @Override
    public void sendLocalizedMessage(@NonNull String key, Object... objects) {

    }
}
