package net.skykingdoms.backend.base.data.stat;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;

import java.util.*;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(exclude = "selectQuery")
@Getter
public final class StatsMode {

    private static final String SELECT = "SELECT %fields% " +
            "FROM `Stats`.`%table%` " +
            "WHERE `Id` = ? AND (`Day` >= ? OR `Day` = 0);";

    final String table;
    final LinkedHashMap<String, StatsField> fields;

    String selectQuery;

    StatsMode(final @NonNull String tableName) {
        this.table = tableName.toUpperCase();
        this.fields = new LinkedHashMap<>();
    }

    void addField(final @NonNull StatsField field) {
        this.fields.put(field.getName().toLowerCase(), field);
    }

    void buildSelectQuery() {
        StringJoiner joiner = new StringJoiner("`, `", "`", "`"); // `day`, `quests`, `time`

        for (String s : fields.keySet()) {
            joiner.add(s);
        }

        this.selectQuery = SELECT.replace("%fields%", joiner.toString()).replace("%table%", this.table);
    }

    public Collection<StatsField> getFields() {
        return Collections.unmodifiableCollection(fields.values());
    }

    public StatsField getField(final @NonNull String field) {
        StatsField result = fields.get(field.toLowerCase());

        if (result == null) {
            System.err.println("Warning: StatsField for '" + field + "' not found in " + this);
        }

        return result;
    }

    // same as getField(), but does not print warnings
    public StatsField getOptionalField(final @NonNull String field) {
        return fields.get(field.toLowerCase());
    }

    public StatsField getDay() {
        return getField("Day");
    }

    public StatsField getTime() {
        return getField("time");
    }

    public String getDisplayName() {
        val table = this.table;

        val master = GameType.valueOf(table, null);
        if (master != null) {
            return master.getHumanName();
        }

        val mini = GameMode.valueOf(table, null);
        if (mini != null) {
            return mini.getHumanName();
        }

        return table;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && getClass() == o.getClass() && Objects.equals(table, ((StatsMode) o).table);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table);
    }

}
