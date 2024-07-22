package net.skykingdoms.backend.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class ServerInactiveEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    public ServerInactiveEvent() {
        super(true);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlerList;
    }
}
