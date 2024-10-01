package net.skykingdoms.backend.megamind.plugin;

import lombok.Getter;
import net.skykingdoms.api.megamind.plugin.MegamindPlugin;
import net.skykingdoms.backend.megamind.MegamindApp;

import java.nio.file.Path;

/**
 * Represents any Plugin that may be loaded at runtime to enhance existing
 * functionality.
 */
@Getter
public class Plugin extends MegamindPlugin {
    /**
     * @return the connection folder of this plugin
     */

    @Override
    public final Path getDataPath() {
        return MegamindApp.INSTANCE.plugins().resolve(getDescription().getName());
    }
}
