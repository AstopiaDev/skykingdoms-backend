package net.skykingdoms.backend.base.data.state;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.util.Consumer;
import net.skykingdoms.backend.base.gamer.CommonGamer;

import java.util.Arrays;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum NetworkState {

    /**
     * Common server state that allows everyone to join
     */
    OPEN("motd", Consumer.empty(), gamer -> {
        gamer.sendMessage("§cYou might experience a lot of bugs, we have a new system that is still in development.\n §cPlease report any bugs to our staff team.");
    }),

    /**
     * Development server. Only STAFF is able to join
     */
    DEVELOPMENT("motd_devserver", gamer -> {
        if (!gamer.getName().equals("TheJavaCat") && !gamer.isStaff()) {
            gamer.localizedDisconnect("networkstate_development_disconnect_online");
        }
    }, gamer -> {
        if (!gamer.getName().equals("TheJavaCat") && !gamer.isStaff()) {
            gamer.localizedDisconnect("networkstate_development_disconnect");
        }
    }),

    /**
     * Maintenance state. Only staff can join
     */
    MAINTENANCE("motd_maintenance", gamer -> {
        if (gamer.getName().equalsIgnoreCase("iliketesting123")) {
            return;
        }
        if (!gamer.isStaff()) {
            gamer.localizedDisconnect("networkstate_maintenance_disconnect_online");
        }
    }, gamer -> {
        if (gamer.getName().equalsIgnoreCase("iliketesting123")) {
            return;
        }
        if (!gamer.isStaff()) {
            gamer.localizedDisconnect("networkstate_maintenance_disconnect");
        }
    })

    ;

    /**
     * Bungee MOTDs
     */
    String motd;

    /**
     * Consumer that is activated by enabling the specified state
     */
    Consumer<CommonGamer> onEnable;

    /**
     * Consumer that is called when a player
     * tries to connect to the network
     */
    Consumer<CommonGamer> onJoin;

    public static final NetworkState[] VALUES = values();

    public static NetworkState getById(final int id){
        return VALUES[id];
    }

    public static NetworkState getByName(final @NonNull String name) {
        return Arrays.stream(VALUES)
                .filter(state -> state.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

}
