package net.skykingdoms.backend.megamind.event.gamer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class PlayerBuildModeEvent extends Event {

    Gamer gamer;

    boolean buildMode;

}