package net.skykingdoms.backend.bukkit.security.impl;

import com.destroystokyo.paper.event.player.PlayerHandshakeEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.val;
import net.skykingdoms.backend.protocol.logging.Logging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public final class SecurityHandshakeListener implements Listener {

/*    private static final String SERVER_VERSION = getServerVersion();
    private static Method getHandle;
    private static Method getProfile;
    static {
        try {
            getHandle = getCraftPlayerClass().getDeclaredMethod("getHandle");
            getProfile = getEntityHumanClass().getDeclaredMethod("getProfile");
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    private static String getServerVersion() {
        Class<?> instance = Bukkit.getServer().getClass();
        if (!instance.getSimpleName().equals("CraftServer")) {
            return ".";
        }
        if (instance.getName().equals("org.bukkit.craftbukkit.CraftServer")) {
            // Non versioned class
            return ".";
        } else {
            String version = instance.getName().substring("org.bukkit.craftbukkit".length());
            return version.substring(0, version.length() - "CraftServer".length());
        }
    }*/

    @EventHandler
    public void onHandshake(AsyncPlayerPreLoginEvent e) {
/*       try {*/
           PlayerProfile playerProfile = e.getPlayerProfile();
           Logging.IMP.info("Player {} has profile {}", e.getName(), playerProfile.hasProperty("security-token"));
 /*      } catch (Exception e) {
           e.printStackTrace();
       }*/

/*        if (decoded instanceof BungeeCordHandshake.Fail) {
            BungeeCordHandshake.Fail fail = (BungeeCordHandshake.Fail) decoded;
            var ip = e.getSocketAddressHostname();

            Logging.IMP.warn("[Security] Denying connection from {} {} - reason: {}", e.getUniqueId(), fail.describeConnection(), fail.reason().name());

            if (fail.reason() == BungeeCordHandshake.Fail.Reason.INVALID_HANDSHAKE) {
                e.setFailMessage(ChatColor.RED + "Internal instance error\n[SEC-2]");
            } else {
                e.setFailMessage(ChatColor.RED + "Internal instance error\n[SEC-3-" + fail.reason().name() + "]");
            }

            e.setFailed(true);
            return;
        }

        val data = (BungeeCordHandshake.Success) decoded;
        e.setServerHostname(data.serverHostname());
        e.setSocketAddressHostname(data.socketAddressHostname());
        e.setUniqueId(data.uniqueId());
        e.setPropertiesJson(data.propertiesJson());*/
    }

/*    private static @NotNull Class<?> getCraftPlayerClass() throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit" + SERVER_VERSION + "entity.CraftPlayer");
    }

    private static @NotNull Class<?> getEntityHumanClass() throws ClassNotFoundException {
        return Class.forName("net.minecraft.instance" + SERVER_VERSION + "EntityHuman");
    }*/

}