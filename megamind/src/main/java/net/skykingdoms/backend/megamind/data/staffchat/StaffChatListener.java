package net.skykingdoms.backend.megamind.data.staffchat;

import lombok.val;
import net.skykingdoms.backend.base.permission.Group;
import net.skykingdoms.backend.base.permission.PermissionType;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.api.megamind.event.EventHandler;
import net.skykingdoms.backend.api.megamind.event.Listener;
import net.skykingdoms.backend.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.backend.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.backend.megamind.manager.StaffChatManager;

/**
 * @author Vihara
 */
public final class StaffChatListener implements Listener {

    public StaffChatListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onGamerJoin(final GamerJoinEvent event) {
        val gamer = event.getGamer();
        if (gamer.getRanks().getRanks(PermissionType.MEDIA).isEmpty()
                && !gamer.getRanks().canApply(Group.BUILDER)) {
            return;
        }

        StaffChatManager.INSTANCE.sendMessage(gamer.getDisplayName() + ChatColor.WHITE + " connected");
    }

    @EventHandler
    public void onLeave(final GamerQuitEvent event) {
        val gamer = event.getGamer();
        if (gamer.getRanks().getRanks(PermissionType.MEDIA).isEmpty()
                && !gamer.getRanks().canApply(Group.BUILDER)) {
            return;
        }

        StaffChatManager.INSTANCE.sendMessage(gamer.getDisplayName() + ChatColor.WHITE + " disconnected");
    }

}
