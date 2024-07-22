package net.skykingdoms.backend.base.util.time;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class Cooldown {

    int initialTime;
    int availableAt;

    public boolean isActive() {
        return availableAt > TimeUtil.getNow();
    }

}
