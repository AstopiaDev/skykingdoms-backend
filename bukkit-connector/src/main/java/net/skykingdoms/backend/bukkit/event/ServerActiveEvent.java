package net.skykingdoms.backend.bukkit.event;

import lombok.Getter;
import net.skykingdoms.backend.protocol.packet.BossProcessor;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public final class ServerActiveEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final BossProcessor processor;

    public ServerActiveEvent(BossProcessor processor) {
        super(true);

        this.processor = processor;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlerList;
    }
}
