package net.skykingdoms.backend.megamind.plugin.event;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.protocol.packet.play.PostEventPacket;
import net.skykingdoms.backend.api.megamind.event.IEventBus;
import net.skykingdoms.backend.api.megamind.event.Event;
import net.skykingdoms.backend.megamind.manager.ServiceManager;
import net.skykingdoms.backend.megamind.server.impl.ServiceServer;
import net.skykingdoms.backend.protocol.logging.Logging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EventBus implements IEventBus {
    Map<Class<?>, Set<String>> bySubscribedServices = new HashMap<>();

    @Override
    public void onPost(@NonNull Event event) {
        Set<String> subscribedServices = bySubscribedServices.get(event.getClass());
        if (subscribedServices != null) {
            for (val serviceName : subscribedServices) {
                ServiceServer service = ServiceManager.INSTANCE.getService(serviceName);

                if (service == null) {
                    continue;
                }

                service.getChannel().sendPacket(new PostEventPacket(event.getClass(), event));
                Logging.IMP.info("Sending a packet to service {}", serviceName);
            }
        }
    }

    public void register(final @NonNull String service, final @NonNull String[] eventNames) {
        try {
            for (String eventName : eventNames) {
                Class<?> eventClazz = Class.forName(eventName);
                bySubscribedServices
                        .computeIfAbsent(eventClazz, k -> new HashSet<>())
                        .add(service);
            }
        } catch (ClassNotFoundException e) {
            Logging.IMP.error("Class not found! {}", e);
        }
    }

    public void unregister(final @NonNull String service) {
        for (Set<String> services : bySubscribedServices.values()) {
            services.remove(service);
        }
    }
}
