package net.skykingdoms.backend.megamind.data.staffchat;

import lombok.val;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.permission.PermissionType;
import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.MegamindApp;
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
        val gamer = (CommonGamer) event.getGamer();
        if (gamer.getRanks().getRanks(PermissionType.MEDIA).isEmpty()
                && !gamer.getRanks().canApply(Group.BUILDER)) {
            return;
        }

        StaffChatManager.INSTANCE.sendMessage(gamer.getColoredName() + ChatColor.AQUA + " is now online");
    }

    @EventHandler
    public void onLeave(final GamerQuitEvent event) {
        val gamer = (CommonGamer) event.getGamer();
        if (gamer.getRanks().getRanks(PermissionType.MEDIA).isEmpty()
                && !gamer.getRanks().canApply(Group.BUILDER)) {
            return;
        }

        StaffChatManager.INSTANCE.sendMessage(gamer.getColoredName() + ChatColor.AQUA + " is now offline");
    }

}
