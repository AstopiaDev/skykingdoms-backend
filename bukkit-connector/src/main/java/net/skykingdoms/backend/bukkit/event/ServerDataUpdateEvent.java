package net.skykingdoms.backend.bukkit.event;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class ServerDataUpdateEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    FluentBukkitDataImpl data;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlerList;
    }

}
