package net.skykingdoms.backend.megamind.data.group;


import lombok.val;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.event.PlayerRedirectEvent;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Vihara
 */
public final class GroupListener implements Listener {

    public GroupListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onJoin(GamerJoinEvent event) {
        val gamer = (Gamer) event.getGamer();
        val language = (Language) gamer.getLanguage();

        val now = LocalDateTime.now();

        gamer.checkExpiredRanks();

        int amount = 0;

        for (val rank : gamer.getRanks()) {
            val expiring = rank.getExpiringTime();

            if (expiring != null) {
                long timeUntilExpire = java.time.Duration.between(now, expiring)
                        .toMillis();

                if (timeUntilExpire <= TimeUnit.DAYS.toMillis(3)) {
                    gamer.sendLocalizedMessage(
                            gamer.isModern(),
                            "group_expires",
                            "%rank%", rank.getGroup().getDisplayName(),
                            "%time%", Duration.durationWordsMs(language, timeUntilExpire)
                    );

                    amount++;
                }
            }
        }

        if (amount > 0) {
            gamer.sendLocalizedMessage(gamer.isModern(), "group_expires_information");
        }
    }

    @EventHandler
    public void onRedirect(PlayerRedirectEvent event) {
        event.getGamer().checkExpiredRanks();
    }

}
