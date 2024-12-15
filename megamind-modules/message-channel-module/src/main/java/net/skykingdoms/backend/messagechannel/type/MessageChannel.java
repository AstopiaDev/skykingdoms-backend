package net.skykingdoms.backend.messagechannel.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.HashSet;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public abstract class MessageChannel implements IMessageChannel {
    HashSet<Gamer> gamers;
}
