package net.skykingdoms.minimind.social.event;

import lombok.NonNull;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.event.IEventBus;
import net.skykingdoms.minimind.social.party.PartyListener;

public class EventBus implements IEventBus {
    public EventBus() {
        this.register(new PartyListener());
    }

    @Override
    public void onPost(@NonNull Event event) {

    }
}
