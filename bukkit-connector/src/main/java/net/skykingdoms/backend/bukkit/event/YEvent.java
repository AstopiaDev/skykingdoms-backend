package net.skykingdoms.backend.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class YEvent extends Event {

    public YEvent(boolean async) {
        super(async);
    }

    public YEvent() {
        super();
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
