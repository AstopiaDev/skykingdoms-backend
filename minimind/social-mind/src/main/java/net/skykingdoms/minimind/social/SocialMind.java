package net.skykingdoms.minimind.social;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerJoinEvent;
import net.skykingdoms.api.megamind.event.impl.gamer.GamerQuitEvent;
import net.skykingdoms.minimind.Minimind;
import net.skykingdoms.minimind.social.event.EventBus;
import net.skykingdoms.minimind.social.listener.CoreListener;
import net.skykingdoms.minimind.social.party.PartyManager;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
public class SocialMind extends Minimind {
    static SocialMind instance = null;

    private SocialMind() { super("social"); }

    public static SocialMind getInstance() {
        if (instance == null) {
            instance = new SocialMind();
        }
        return instance;
    }

    EventBus eventBus;

    @Override
    public void preBootstrap() {
        new CoreListener();

        this.eventBus = new EventBus();
        this.serviceData.setEventSubscriptions(
                GamerJoinEvent.class,
                GamerQuitEvent.class
        );
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