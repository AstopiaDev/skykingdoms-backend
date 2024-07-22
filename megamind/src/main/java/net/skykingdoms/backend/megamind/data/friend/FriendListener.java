package net.skykingdoms.backend.megamind.data.friend;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.data.settings.SettingType;
import net.skykingdoms.backend.base.data.settings.enums.Notifications;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.event.EventHandler;
import net.skykingdoms.backend.megamind.api.plugin.Listener;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.event.gamer.GamerJoinEvent;
import net.skykingdoms.backend.megamind.event.gamer.GamerQuitEvent;
import net.skykingdoms.backend.megamind.event.gamer.PlayerVanishEvent;

import java.util.List;

/**
 * @author Vihara
 */
public final class FriendListener implements Listener {

    public FriendListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onPlayerQuit(GamerQuitEvent event) {
        Gamer gamer = event.getGamer();

        FriendManager.INSTANCE.handlePlayerQuit(gamer);

        notifyFriendStatus(gamer, false, true);
    }


    @EventHandler
    public void onPlayerVanish(PlayerVanishEvent event) {
        Gamer gamer = event.getGamer();

        notifyFriendStatus(gamer, true, false);
    }

    private void notifyFriendStatus(final @NonNull Gamer gamer,
                                    final boolean online,
                                    final boolean ignoreVanished) {
        gamer.loadFriends();
        for (val friend : gamer.getOnlineFriends()) {
            if (friend == null || friend == gamer) {
                continue;
            }

            val friendNotifySetting = (Notifications) friend.getSetting(SettingType.FRIEND_NOTIFICATIONS).getValue();

            if (friendNotifySetting == Notifications.NONE) {
                continue;
            }

            // TODO: Best friends realization for notifications

            friend.sendLocalizedMessage(online ? "friend_join_the_game" : "friend_left_the_game",
                    "%name%", gamer.getDisplayName()
            );
        }
    }

    @EventHandler
    public void onPlayerJoin(GamerJoinEvent event) {
        Gamer gamer = event.getGamer();

        notifyFriendStatus(gamer, true, true);

        List<Gamer> online = gamer.getOnlineFriends();

        if (online.isEmpty()) {
            return;
        }

//        StringBuilder list = new StringBuilder(gamer.getLanguage().getMessage("friends_online"));

        int count = 0;

        for (Gamer target : online) {
            val server = target.getCurrentServer();

            if (server == null) {
                continue;
            }

            if (count >= 5) {
//                list.append("\n").append(
//                        gamer.getLanguage().getMessage("friend_online_others_entry", "%count%", online.size() - count)
//                );

                break;
            }

//            list.append("\n").append(gamer.getLanguage().getMessage("friend_online_entry", "%server%", server.getName(), "%name%", target.getDisplayName()));

            count++;
        }

//        gamer.sendMessage(list.toString());
    }
}
