package net.skykingdoms.backend.base.data.tags;

public enum SeasonObtained {
    IMPOSSIBLE,
    PURCHASABLE,
    HALLOWEEN,
    WINTER,
    VALENTINES,
    EASTER,
    SUMMER,
    PARKOUR,
    EASTER_AND_ST_PATRICKS,
    ST_PATRICKS,
    SPRING,
    LUCKY,
    SERVER_ANNIVERSARY,
    BIRTHDAY,
    ;

    public static SeasonObtained fromString(String name) {
        for (SeasonObtained type : VALUES) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public SeasonObtained next() {
        int next = ordinal() + 1;

        return VALUES.length == next ? null : VALUES[next];
    }

    public static final SeasonObtained[] VALUES = values();
}
