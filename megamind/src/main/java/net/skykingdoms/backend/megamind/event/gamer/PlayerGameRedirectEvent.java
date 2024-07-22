package net.skykingdoms.backend.megamind.event.gamer;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class PlayerGameRedirectEvent extends Event {

    private final Gamer gamer;

    private final List<Gamer> additionalGamerList = new ArrayList<>();

}
