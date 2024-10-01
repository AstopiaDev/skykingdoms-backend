package net.skykingdoms.backend.megamind.matchmaking;

import com.google.common.collect.Lists;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameState;
import net.skykingdoms.backend.base.data.stat.StatisticPeriod;
import net.skykingdoms.backend.base.util.optionals.Optional;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.impl.GameServer;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Primary matchmaking module that orchestrates the players along the network
 * Currently used to support skywars ONLY. So we don't play any expanding
 * of this module, and there is NO abstraction :P
 *
 */
public final class MatchmakingManager {

    public static final MatchmakingManager INSTANCE = new MatchmakingManager();

    /**
     * Epoch month number
     */
    int currentMonth = 0;

    public void postBootstrap() {
        currentMonth = Integer.parseInt(MegamindApp.INSTANCE.getProperty("matchmaking.wipe", "" + Calendar.getInstance().get(Calendar.MONTH)));
        Logging.IMP.info("[Matchmaking] Latest wipe month: {} (Current: {})",
                currentMonth, Calendar.getInstance().get(Calendar.MONTH));

        MegamindApp.INSTANCE.scheduler().scheduleAtFixedRate(() -> {
            val newMonth = Calendar.getInstance().get(Calendar.MONTH);
            if (currentMonth != newMonth) {
                Logging.IMP.info("[Matchmaking] Caught the beginning of new month. C: {} @ N: {}",
                        currentMonth, newMonth);

                // resetting rating to 0 for everyone
                //PropertySqlHandler.INSTANCE.resetGlobalProperty(PropertyType.RA, 0);

                // persisting changed month
                currentMonth = newMonth;
                MegamindApp.INSTANCE.setProperty("matchmaking.wipe", newMonth);

                Logging.IMP.info("[Matchmaking] Rating and shit wiped! New month: {}",
                        newMonth);
            }
        }, 0, 1, TimeUnit.SECONDS);

        StatisticPeriod.MONTH.getDurationUntilEnd();
        Logging.IMP.info("[Matchmaking] Started ranked season wipe thingy...");
    }

    /**
     * Experimental, actually :nerd:
     */
    public net.skykingdoms.backend.base.util.optionals.Optional<GameServer> getOptimalServer(final @NonNull Gamer gamer) {
        val rating = gamer.getPropertyOrDefault("achievement_points", 0);

        // GameServer -> Average rating
        Map<GameServer, Integer> rankedServers = ServerManager.INSTANCE.getGameServers().stream()
                /*.filter(gameServer -> gameServer.getGameMode() == GameMode.SURVIVAL_MODERN)*/
                .collect(Collectors.toMap(gameServer -> gameServer, gameServer -> {
                    val overall = gameServer.getGamers().stream().mapToInt(iGamer -> iGamer.getProperty("achievement_points")).sum();
                    return overall / Math.max(1, gameServer.getGamers().size()); // avg
        }));

        GameServer ret = null; // not initialized yet, there might be no servers for ranked :/
        var servers = rankedServers.entrySet().stream().filter(s -> s.getKey().canJoin() && s.getKey().getCurrentState() == GameState.STAND_BY).sorted((a, b) -> Integer.compare(b.getValue(), a.getValue())).collect(Collectors.toList());
        for (val server : servers) {
            if (rating <= server.getValue()) {
                ret = server.getKey();

            } else if (server.getKey().getPlayerIds().length == 0) {
                ret = server.getKey();
            }
        }

        // fuck it, just getting random server with the smallest avg rating for our gamer
        servers = Lists.reverse(servers);

        if (ret == null && servers.size() > 0) {
            val avgRating = servers.get(0).getValue();
            val server = servers.get(0).getKey();

            // if avg rating is significantly different
            // from our dude's, we notify him
            if (avgRating > rating + 100) {
                gamer.sendLocalizedMessage(gamer.isModern(), "skyclash_ranked_matchmaking_warn",
                        "%average%", avgRating,
                        "%delta%", avgRating - rating);
            }

            ret = server;
        }

        if (ret == null) {
            Logging.IMP.warn("[Matchmaking] Couldn't find a server for {} @ Rating: {}",
                    gamer.getName(), rating);
        } else {
            Logging.IMP.info("[Matchmaking] Found a server for {} @ Rating: {} @ Server: {}",
                    gamer.getName(), rating, ret.getName());
        }

        // returning what we've got
        return Optional.ofNullable(ret);
    }

}
