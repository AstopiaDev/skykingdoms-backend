package net.skykingdoms.backend.velocity.event;

import com.velocitypowered.api.proxy.Player;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

/**
 * Invoked when a player executes coordinator command
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class PlayerCoreCommandEvent {

    Player player;
    String command;

    @Setter @NonFinal boolean cancelled;

}
