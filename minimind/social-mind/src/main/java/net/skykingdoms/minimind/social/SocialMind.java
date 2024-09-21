package net.skykingdoms.minimind.social;

import net.skykingdoms.minimind.Minimind;
import net.skykingdoms.minimind.social.party.PartyListener;
import net.skykingdoms.minimind.social.party.PartyManager;

public class SocialMind extends Minimind {
    private static SocialMind instance = null;

    private SocialMind() { super("social"); }

    public static SocialMind getInstance() {
        if (instance == null) {
            instance = new SocialMind();
        }
        return instance;
    }

    @Override
    public void preBootstrap() {
        this.serviceData.setEventSubscriptions(new String[] {
                "net.skykingdoms.backend.megamind.event.gamer.GamerJoinEvent"
        });
    }

    @Override
    public void postBootstrap() {
        PartyManager.INSTANCE.postBootstrap();
    }

    @Override
    public void shutdown() {
        PartyManager.INSTANCE.persistParties();
    }
}