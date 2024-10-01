package net.skykingdoms.backend.bukkit.startup.core;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.logging.Logging;

import java.util.HashMap;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public final class TestFlowData {

    // Indicates where to put files from testflow
    Map<String, String> relocations = new HashMap<>();

    boolean debug;
    int debugPort;

    // adds relocation entry
    public void addRelocation(@NonNull String fileName,
                              final @NonNull String target) {
        // todo: refactor
        fileName = fileName.replace("]", "");
        fileName = fileName.replace("[", "");

        Logging.IMP.info("[boot/debug] added relocation: {} -> {}", fileName, target);
        relocations.put(fileName, target);
    }

}
