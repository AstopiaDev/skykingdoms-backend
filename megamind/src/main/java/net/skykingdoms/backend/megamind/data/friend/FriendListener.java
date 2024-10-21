package net.skykingdoms.backend.megamind.data.friend;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.enums.Notifications;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.PlayerVanishEvent;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

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
        Gamer gamer = (Gamer) event.getGamer();

        FriendManager.INSTANCE.handlePlayerQuit(gamer);

        if (!gamer.isVanished()) {
            notifyFriendStatus(gamer, false, true);
        }
    }


    @EventHandler
    public void onPlayerVanish(PlayerVanishEvent event) {
        Gamer gamer = (Gamer) event.getGamer();

        notifyFriendStatus(gamer, !event.isVanished(), false);
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
                    "%name%", gamer.getColoredName()
            );
        }
    }

    @EventHandler
    public void onPlayerJoin(GamerJoinEvent event) {
        Gamer gamer = (Gamer) event.getGamer();

        if (!gamer.isVanished()) {
            notifyFriendStatus(gamer, true, true);
        }

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
