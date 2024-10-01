package net.skykingdoms.api.backend.command.cooldown;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class WrappedCooldown {

    public static final WrappedCooldown EMPTY = new WrappedCooldown(0, false);

    private final int value;
    private final boolean currentServerOnly;

}
