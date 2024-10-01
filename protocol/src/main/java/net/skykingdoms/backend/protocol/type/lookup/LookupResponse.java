package net.skykingdoms.backend.protocol.type.lookup;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public final class LookupResponse implements Serializable<LookupResponse> {

    LookupServer[] servers;

    public LookupResponse() {
        this.servers = new LookupServer[0];
    }

    public LookupResponse(@NonNull LookupServer... servers) {
        this.servers = servers;
    }

    public int getServerCount() {
        return servers.length;
    }

    public boolean isEmpty() {
        return getServerCount() == 0;
    }

    public int getTotalOnline() {
        int total = 0;

        for (LookupServer server : servers) {
            total += server.getOnline();
        }

        return total;
    }

    public LookupServer first() {
        return isEmpty() ? null : servers[0];
    }

    public static LookupResponse merge(final @NonNull LookupResponse first,
                                       final @NonNull LookupResponse second) {
        val firstServers = first.getServers();
        val secondServers = second.getServers();

        // Создаём лист всех серверов
        val totalSortedServers = new LookupServer[firstServers.length + secondServers.length];
        System.arraycopy(firstServers, 0, totalSortedServers, 0, firstServers.length);
        System.arraycopy(secondServers, 0, totalSortedServers, firstServers.length, secondServers.length);

        // Сортируем по "новизне", т.е самые новые элементы будут вначале листа
        // (чем меньше время, тем старше элементы, получается нам  нужно сделать их в обратном порядке)
        Arrays.sort(totalSortedServers, Comparator.comparingLong(LookupServer::getTime).reversed());

        Map<LookupServer, LookupServer> merged = new LinkedHashMap<>();

        totalServerLoop:
        for (LookupServer server : totalSortedServers) {
            for (LookupServer mergedServer : merged.keySet()) {
                // сравниваем имена
                if (mergedServer.getName().equals(server.getName())) {
                    int mask = mergedServer.getMask();
                    int oldMask = server.getMask();
                    int totalMask = mask | oldMask;

                    // new: 000001  110000
                    // old: 110001  000001
                    // mer: 110000  000001

                    int mergedMask = ~mask;

                    if (mergedMask != 0) {
                        for (LookupFlag flag : LookupFlag.fromMask(mergedMask & LookupFlag.MASK)) {
                            flag.mergeField(server, mergedServer);
                        }
                    }

                    mergedServer.setMask(totalMask);

                    continue totalServerLoop;
                }
            }

            LookupServer copy = server.copy();
            merged.put(copy, copy);
        }

        return new LookupResponse(merged.keySet().toArray(new LookupServer[0]));
    }

    @Override
    public LookupResponse serialize(final @NonNull ByteBuf out) {
        PacketUtils.writeSerializableArray(out, servers);
        return this;
    }

    @Override
    public LookupResponse deserialize(final @NonNull ByteBuf in) {
        servers = PacketUtils.readSerializableArray(in, LookupServer[]::new, LookupServer::new);
        return this;
    }

}
