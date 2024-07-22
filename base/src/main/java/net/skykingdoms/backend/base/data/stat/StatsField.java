package net.skykingdoms.backend.base.data.stat;


import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.base.util.time.DurationField;
import net.skykingdoms.backend.base.util.time.DurationFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
@ToString(exclude = {"mode", "addPlayerStatQuery", "topFromDayQuery", "allTimeTopQuery"})
public class StatsField {

    /*
     * Формат комента к stats полям в MySQL:
     *
     * key=value
     * key
     *
     * Если значение ключа не указано, значение будет 'true'
     *
     * Допустимые ключи:
     * - desc: описание поля. Если не указано, поле не отображается в стате и в GUI и не может быть квестовым
     * - format: формат поля при выводе в стату/гуи. Допустимые значения: plain, duration_sec
     * - quest_bounds: значения для квеста. Формат: 'min/max'.
     * min -- минимальное значение для квеста,
     * max -- максимальное
     * - quest_step: шаг, с которым варьируется значение в диапазоне quest-bounds. По умолчанию 1.
     * - quest_experience: опыт, которым оценивается 1 единица этой статистики
     * - quest_coins: опыт, которым оценивается 1 единица этой статистики
     * - temporary_display_mode: формат отображения временной статистики. Например, чтобы лучшая серия убийств отображалась не суммой, а максимальным числом
     * */

    private static final String ADD_PLAYER_STAT = "INSERT INTO `Stats`.`%table%` (`Id`, `Day`, `%field%`) " +
            "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `%field%` = `%field%` + ?;";

    private static final String GET_TOP_FROM_DAY = "SELECT `Id`, %function%(`%field%`) AS `Total` " +
            "FROM `Stats`.`%table%` " +
            "WHERE `Day` >= ? " +
            "GROUP BY `Id` " +
            "ORDER BY `Total` %sort_mode%;";

    private static final String GET_ALL_TIME_TOP = "SELECT `Id`, MAX(`%field%`) AS `Total` " +
            "FROM `Stats`.`%table%` " +
            "WHERE `Day` = 0 " +
            "GROUP BY `Id` " +
            "ORDER BY `Total` %sort_mode%;";

    private final StatsMode mode;

    private final String name;

    private final Map<String, String> data = new HashMap<>();

    private final String addPlayerStatQuery, topFromDayQuery, allTimeTopQuery;

    public StatsField(@NonNull StatsMode mode, @NonNull String name, @NonNull String description) {
        this.mode = mode;
        this.name = name;

        parseFieldData(description);

        this.addPlayerStatQuery = ADD_PLAYER_STAT.replace("%table%", mode.getTable()).replace("%field%", name);
        this.topFromDayQuery = GET_TOP_FROM_DAY.replace("%table%", mode.getTable()).replace("%field%", name);
        this.allTimeTopQuery = GET_ALL_TIME_TOP.replace("%table%", mode.getTable()).replace("%field%", name);
    }

    public String getTopQuery(String function, boolean ascending) {
        return topFromDayQuery
                .replace("%function%", function)
                .replace("%sort_mode%", ascending ? "ASC" : "DESC");
    }

    public String getAllTimeTopQuery(boolean ascending) {
        return allTimeTopQuery
                .replace("%sort_mode%", ascending ? "ASC" : "DESC");
    }

    private void parseFieldData(@NonNull String desc) {
        if (desc.isEmpty()) {
            return;
        }

        if (!desc.contains("=") && !desc.contains("\n") && !desc.contains(";")) {
            this.data.put("desc", desc);
            return;
        }

        for (String pair : desc.split("[\n;]")) {
            pair = pair.trim();

            if (pair.isEmpty() || pair.startsWith("#")) {
                continue;
            }

            int eq = pair.indexOf('=');
            if (eq == -1) {
                this.data.put(pair, "true");
            } else {
                this.data.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
            }
        }
    }

    public String getDataValue(final @NonNull String key) {
        return data.get(key);
    }

    public String getDataValue(final @NonNull String key,
                               final String def) {
        return data.getOrDefault(key, def);
    }

    public String getDescription() {
        return data.get("desc");
    }

    public String getDescription(final @NonNull Language language) {
        String descriptionKey = getDescription();

        return language.isMessageTranslated(descriptionKey) ? language.getMessage(false, descriptionKey) : descriptionKey;
    }

    public boolean hasDescription() {
        return data.containsKey("desc");
    }

    public boolean isCached() {
        return data.containsKey("cached");
    }

    public String getTemporaryDisplayMode() {
        return getDataValue("temporary_display_mode", "sum");
    }

    public String format(final int value) {
        switch (getDataValue("format", "plain")) {
            case "duration_sec":
                val params = new Duration.FormatParams();
                params.sourceUnit = TimeUnit.SECONDS;
                params.locale = DurationFormatter.RU;
                params.min = DurationField.MIN;
                params.max = DurationField.HOUR;

                return Duration.format(value, params);

            case "plain":
            default:
                return Integer.toString(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        if (this == o) {
            return true;
        }

        StatsField that = (StatsField) o;

        return Objects.equals(mode, that.mode) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, name);
    }

}