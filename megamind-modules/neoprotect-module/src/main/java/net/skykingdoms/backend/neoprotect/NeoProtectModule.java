package net.skykingdoms.backend.neoprotect;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.plugin.Plugin;
import net.skykingdoms.backend.neoprotect.command.NeoBackendsCommand;
import net.skykingdoms.backend.neoprotect.listener.ProxyDeploymentListener;
import net.skykingdoms.backend.neoprotect.manager.NeoProtectManager;
import net.skykingdoms.backend.protocol.logging.Logging;

@FieldDefaults(level = AccessLevel.PRIVATE)
@SuppressWarnings("DeprecatedIsStillUsed")
@Getter
public final class NeoProtectModule extends Plugin {

    // ------------------------------------------------------
    @Deprecated
    public static NeoProtectModule INSTANCE;
    // ------------------------------------------------------

    /**
     * NeoProtect API key
     */
    String api;

    /**
     * NeoProtect server shield ID
     */
    String serverId;

    @Setter String backendGroupId;

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveDefaultConfig();
        loadConfig();

        this.api = getConfig().getString("api-token");
        this.serverId = getConfig().getString("server-id");

        NeoProtectManager.INSTANCE.postBootstrap();

        MegamindApp.INSTANCE.commandManager().addCommand(NeoBackendsCommand.class);

        new ProxyDeploymentListener(this);

        val backends = NeoProtectManager.INSTANCE.getLoadedBackends();
        Logging.IMP.info("In total {} backends loaded (local){}", backends.size(), !backends.isEmpty() ? ":" : "");
        for (val backend : backends) {
            Logging.IMP.info("{} -> {}:{}", backend.getId(), backend.getIp(), backend.getPort());
        }
    }
}
