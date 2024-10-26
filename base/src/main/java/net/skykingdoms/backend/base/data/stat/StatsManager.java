package net.skykingdoms.backend.base.data.stat;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.base.util.time.DurationField;
import net.skykingdoms.backend.base.util.time.DurationFormatter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Getter
public final class StatsManager {

    public static final StatsManager INSTANCE = new StatsManager();

    private final Duration.FormatParams params = new Duration.FormatParams() {
        {
            sourceUnit = TimeUnit.SECONDS;
            locale = DurationFormatter.RU;
            min = DurationField.MIN;
            max = DurationField.HOUR;
        }
    };

    private final Map<String, StatsMode> modes = new ConcurrentHashMap<>();

    public void postBootstrap() {
        modes.clear();

        for (String table : StatsSqlHandler.INSTANCE.loadTables().join()) {
            StatsMode mode = StatsSqlHandler.INSTANCE.loadMode(table)
                    .join();

            modes.put(table.toUpperCase(), mode);

            Logging.IMP.info("[Statistic] Table {} loaded for {}", table, mode.getDisplayName());
            // Logging.IMP.info("[Statistic] Entries: ({})", table);
        }
    }

    public StatsMode tryGetMode(final String mode) {
        return mode == null ? null : modes.get(mode.toUpperCase());
    }

    public StatsMode tryGetMode(final @NonNull GameMode mini) {
        return tryGetMode(mini.name());
    }

    public StatsMode tryGetMode(final @NonNull GameType master) {
        return tryGetMode(master.name());
    }

    public StatsMode getMode(final @NonNull String mode) {
        StatsMode result = modes.get(mode.toUpperCase());

        if (result == null) {
            new IllegalStateException("Warning: StatsMode for '" + mode + "' not found").printStackTrace();
        }

        return result;
    }

    public StatsMode getMode(final @NonNull GameType mode) {
        return getMode(mode.name());
    }

    public StatsMode getMode(final @NonNull GameMode mini) {
        return getMode(mini.name());
    }

    public Collection<StatsMode> getModes() {
        return modes.values();
    }

}
