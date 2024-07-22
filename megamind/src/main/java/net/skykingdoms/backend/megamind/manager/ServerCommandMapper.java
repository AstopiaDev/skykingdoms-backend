package net.skykingdoms.backend.megamind.manager;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class ServerCommandMapper {

    Map<String, Set<String>> serverCommandsMap = new HashMap<>();

    public void registerCommands(final @NonNull String serverName,
                                 final @NonNull Set<String> commands) {
        serverCommandsMap.put(serverName, commands); // overwriting
    }

    public void flushServerCommands(final @NonNull String serverName) {
        serverCommandsMap.remove(serverName);
    }

    public Set<String> getCommandsForServer(final @NonNull String serverName) {
        return serverCommandsMap.get(serverName);
    }

    public Set<Map.Entry<String, Set<String>>> getCommandsEntry() {
        return serverCommandsMap.entrySet();
    }

}