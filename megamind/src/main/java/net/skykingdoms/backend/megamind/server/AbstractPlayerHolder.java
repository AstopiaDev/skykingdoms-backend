package net.skykingdoms.backend.megamind.server;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.packet.play.S21SetGlobalOnline;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractPlayerHolder extends AbstractRemoteConnectable {

    Set<Gamer> gamers = ConcurrentHashMap.newKeySet();

    @Getter @Setter
    @NonFinal int slots;
    @NonFinal @Setter boolean joinable;

    public AbstractPlayerHolder(final @NonNull String name,
                                final @NonNull RemoteClientConnection connection) {
        super(name, connection);
    }

    public int[] getPlayerIds() {
        val playerIds = new int[gamers.size()];

        var n = 0;

        for (val gamer : gamers) {
            playerIds[n++] = gamer.getGamerId();
        }

        return playerIds.length == n + 1 ? playerIds : Arrays.copyOf(playerIds, n);
    }


    public int getSpace() {
        return this.slots - getPlayerIds().length;
    }

    public double getFillRate() {
        if (this.slots == 0) {
            return 1.0;
        }

        return (double) this.getPlayerIds().length / this.slots;
    }

    public boolean addPlayer(final @NonNull Gamer gamer) {
        return gamers.add(gamer);
    }

    public boolean removePlayer(final @NonNull Gamer gamer) {
        return gamers.remove(gamer);
    }

    public boolean canJoin() {
        return joinable; // todo
    }

    public boolean isNotFull() {
        return getPlayerIds().length < slots;
    }

    public void sendGlobalOnline(final int online) {
        getChannel().sendPacket(new S21SetGlobalOnline(online));
    }
}
