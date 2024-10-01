package net.skykingdoms.backend.megamind.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.protocol.type.duel.FluentDuelRequestData;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Accessors
@Getter
public class DuelRequest {
    long createdAt;
    int senderId;
    int targetId;
    GameMode mode;
    FluentDuelRequestData data;
}
