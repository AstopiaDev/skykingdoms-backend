package net.skykingdoms.backend.megamind.manager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.server.impl.ServiceServer;

import java.util.concurrent.ConcurrentHashMap;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ServiceManager {
    INSTANCE;

    ConcurrentHashMap<String, ServiceServer> serviceServers = new ConcurrentHashMap<>();

    /**
     * Basically, registers a service in the hashmap
     */
    public ServiceServer registerService(final @NonNull ServiceServer service) {
        if (isServiceRegistered(service.getName())) {
            throw new IllegalStateException("Service with this name is already registered!");
        }

        Logging.IMP.info("[Service] Registered service {}", service.getName());

        serviceServers.put(service.getName(), service);

        return service;
    }

    /**
     * Removes a service from set of registered services
     */
    public void removeService(final @NonNull ServiceServer server) {
        Logging.IMP.info("[Service] Unregistered service {}", server.getName());

        MegamindApp.INSTANCE.pluginManager().unregisterListener(server.getName());
        serviceServers.remove(server.getName());
    }

    public boolean isServiceRegistered(String name) {
        return serviceServers.containsKey(name);
    }

    /**
     * Gets a service by their name
     */
    public ServiceServer getService(String service) {
        return serviceServers.get(service);
    }
}
