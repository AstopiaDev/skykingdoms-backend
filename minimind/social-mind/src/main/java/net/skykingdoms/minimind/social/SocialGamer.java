package net.skykingdoms.minimind.social;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;
import net.skykingdoms.backend.api.megamind.gamer.MegamindGamer;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.minimind.command.CommandSender;
import net.skykingdoms.minimind.social.party.Party;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public final class SocialGamer extends MegamindGamer implements CommandSender {
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
        return AbstractGamer.getGamer(playerId);
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
}
