package net.skykingdoms.backend.velocity.command;

import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.velocity.VelocityConnector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is used in tabcomplete purposes.
 * Basically stores command maps for each server registered in network.
 *
 * @author Vihara
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class VelocityCommandCocker {

    public static final VelocityCommandCocker INSTANCE = new VelocityCommandCocker();

    @Setter
    Set<String> commands;

    @Getter @Setter
    Map<ServerInfo, Set<String>> serverCommands = new HashMap<>();

    public Set<String> matchCommands(final String needle, final ServerInfo serverInfo) {
        val proxy = VelocityConnector.INSTANCE.server();
        val pluginManager = proxy.getPluginManager();

        val coreCommands = this.commands.stream();
        val bukkitCommands = serverCommands.get(serverInfo) == null
                ? Stream.<String>empty()
                : serverCommands.get(serverInfo).stream();

        val commandManager = VelocityConnector.INSTANCE.server().getCommandManager();

        val bungeeCommands = commandManager.getAliases().stream();

        // xdlol
        return Stream.concat(Stream.concat(coreCommands, bungeeCommands), bukkitCommands)
                .map("/"::concat)
                .filter(haystack -> haystack.regionMatches(true, 0, needle, 0, needle.length()))
                .collect(Collectors.toSet());
    }

}
