package net.skykingdoms.backend.messagechannel.listener;

import net.skykingdoms.api.megamind.event.EventHandler;
import net.skykingdoms.api.megamind.event.Listener;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerMessageChannelEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.messagechannel.manager.MessageManager;

public class MessageChannelListener implements Listener {
    public MessageChannelListener() {
        MegamindApp.INSTANCE.pluginManager().registerListener(this);
    }

    @EventHandler
    public void onJoin(GamerJoinEvent event) {

    }

    @EventHandler
    public void onQuit(GamerQuitEvent event) {
        Gamer gamer = (Gamer) event.getGamer();
        MessageManager.INSTANCE.unloadGamer(gamer);
    }

    @EventHandler
    public void onMessage(GamerMessageChannelEvent event) {

    }
}
