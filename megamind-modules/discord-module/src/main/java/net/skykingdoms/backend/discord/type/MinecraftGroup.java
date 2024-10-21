package net.skykingdoms.backend.discord.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@Accessors(fluent = true)
@RequiredArgsConstructor
@Getter
public enum MinecraftGroup {
    ZEUS("269117376836927490"),
    INFINITY("269117512086585344"),
    TITAN("269117545523445761"),
    GOD("269117574493503499"),
    DEMIGOD("269117609868263426"),
    LEGEND("269117702998589443"),
    HERO("269117749559689216"),
    DONATOR("269117784632459265"),
    VIP("269117816039276544"),
    LINKED("1218727046000152576");

    String id;

    public static final MinecraftGroup[] VALUES = values();
}
