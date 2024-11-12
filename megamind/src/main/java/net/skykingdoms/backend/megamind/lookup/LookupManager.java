package net.skykingdoms.backend.megamind.lookup;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.megamind.server.impl.GameServer;
import net.skykingdoms.backend.protocol.packet.play.S20LookupResponse;
import net.skykingdoms.backend.protocol.packet.play.S21SetGlobalOnline;
import net.skykingdoms.backend.protocol.server.IConnectable;
import net.skykingdoms.backend.protocol.type.lookup.LookupFlag;
import net.skykingdoms.backend.protocol.type.lookup.LookupResponse;
import net.skykingdoms.backend.protocol.type.lookup.LookupServer;
import net.skykingdoms.backend.protocol.type.lookup.LookupTarget;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class LookupManager {

    //==========================================================
    public static final LookupManager INSTANCE = new LookupManager();
    //==========================================================

    Map<AbstractScalable, Lookup> byServer
            = new ConcurrentHashMap<>();

    Map<LookupFlag, Set<Lookup>> byFlag
            = new EnumMap<>(LookupFlag.class);

    Set<IConnectable> onlineLookups
            = CollectionUtil.createConcurrentSet();

    public LookupManager() {
        Timer timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                for (Lookup lookup : byServer.values()) {
                    lookup.flushQueuedResponse();
                }
            }
        }, 0, 5000);
    }

    private Lookup createOrMerge(final @NonNull AbstractScalable server,
                                 final LookupTarget @NonNull [] targets) {
        Lookup lookup = byServer.get(server);

        if (lookup == null) {
            lookup = new Lookup(server, targets);
        } else {
            lookup = lookup.addTargets(targets);
        }

        byServer.put(server, lookup);

        for (val target : lookup.getTargets()) {
            for (val flag : target.getFlags()) {
                byFlag.computeIfAbsent(flag, __ -> new CopyOnWriteArraySet<>())
                        .add(lookup);
            }
        }

        return lookup;
    }

    public void clearLookups() {
        byServer.clear();
        onlineLookups.clear();
        byFlag.clear();
    }

    public void addLookup(final @NonNull AbstractScalable server,
                          final @NonNull LookupTarget[] targets) {
        Lookup lookup = createOrMerge(server, targets);
        sendLookup(lookup);
    }

    public void handleOnlineChange(final @NonNull AbstractScalable server) {
        if (byServer.isEmpty()) {
            return;
        }

        handleEvent(
                1 | 2, // online update & players update
                server
        );
    }

    public void handleServerCreate(final @NonNull AbstractScalable server) {
        if (byServer.isEmpty()) {
            return;
        }

        handleEvent(LookupFlag.ON_RESULT_UPDATE.getMask(), server);
    }

    public void handleGlobalOnlineChange(final int online) {
        for (val server : onlineLookups) {
            server.getChannel().sendPacket(new S21SetGlobalOnline(online));
        }
    }

    /**
     * IConnectable because ANY client can get this
     */
    public void addOnlineLookup(final @NonNull IConnectable server) {
        onlineLookups.add(server);

        server.getChannel().sendPacket(new S21SetGlobalOnline(Gamer.getTotalOnline()));
    }

    public void handleServerRemove(final @NonNull AbstractScalable server) {
        if (byServer.isEmpty()) {
            return;
        }

        val lookup = byServer.remove(server);

        if (lookup != null) {
            for (val target : lookup.getTargets()) {
                for (val flag : target.getFlags()) {
                    Set<Lookup> lookups = byFlag.get(flag);

                    if (lookups == null)
                        continue;

                    lookups.remove(lookup);

                    if (lookups.isEmpty()) {
                        byFlag.remove(flag);
                    }
                }
            }
        }

        onlineLookups.remove(server);

        handleEvent(LookupFlag.ON_RESULT_UPDATE.getMask(), server);
    }

    public void handleStateChange(final @NonNull GameServer server) {
        handleEvent(LookupFlag.ON_STATE_UPDATE.getMask(), server);
    }

    private void sendLookup(Lookup lookup) {
        Set<LookupServer> servers = new HashSet<>();

        for (val server : ServerManager.INSTANCE.getServers()) {
            for (LookupTarget target : lookup.getTargets()) {
                if (target.isTarget(server.bakeServerData())) {
                    val lookupServer = createLookupServer(server, target.getMask(), true);
                    servers.add(lookupServer);
                }
            }
        }

        if (servers.isEmpty()) {
            return;
        }

        lookup.getServer().getChannel().sendPacket(new S20LookupResponse(new LookupResponse(servers.toArray(new LookupServer[0]))));
    }

    private void handleEvent(final int mask,
                             AbstractScalable server) {
        val lookups = new HashSet<Lookup>();

        // that's bukkitserver instance (redundant)
        // but server MIGHT shut down :(
        if (ServerManager.INSTANCE.getBukkitServer(server.getName()) != null) {
            return;
        }

        for (val flag : LookupFlag.fromMask(mask)) {
            val byFlag = this.byFlag.get(flag);

            if (byFlag != null) {
                lookups.addAll(byFlag);
            }
        }

        boolean allFlags = LookupFlag.hasFlag(mask, LookupFlag.ON_RESULT_UPDATE);

        for (val lookup : lookups) {
            for (val target : lookup.getTargets()) {
                val targetMask = allFlags ? target.getMask() : mask;

                if (target.isTarget(server.bakeServerData())) {
                    val response = new LookupResponse(createLookupServer(server, targetMask, ServerManager.INSTANCE.getServers().stream().anyMatch(s -> s.getName().equals(server.getName()))));
                    lookup.queueResponse(response);
                }
            }
        }
    }

    public LookupServer createLookupServer(AbstractScalable server,
                                           int mask,
                                           boolean online) {
        int[] players = server.getPlayerIds();

        return new LookupServer(
                System.currentTimeMillis(), server.getName(), new ScalableDataImpl(server.getServerScale(), server.getGameType(), server.getGameMode()), online,
                server instanceof GameServer ? ((GameServer) server).getCurrentState() : null,
                server instanceof GameServer ? ((GameServer) server).getMap() : null,
                players.length, server.getSlots(), players,
                mask
        );
    }

}