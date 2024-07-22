package net.skykingdoms.backend.base.data.tags;

public enum YearObtained {
    Y2016,
    Y2017,
    Y2018,
    Y2019,
    Y2020,
    Y2021,
    Y2022,
    Y2023,
    Y2024,
    Y5TH,
    ;

    public static YearObtained fromString(String name) {
        for (YearObtained type : VALUES) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public YearObtained next() {
        int next = ordinal() + 1;

        return VALUES.length == next ? null : VALUES[next];
    }

    public static final YearObtained[] VALUES = values();
}
