package net.skykingdoms.backend.megamind.plugin.event;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.megamind.event.Event;
import net.skykingdoms.api.megamind.event.IEventBus;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.megamind.manager.ServiceManager;
import net.skykingdoms.backend.megamind.server.impl.ServiceServer;
import net.skykingdoms.backend.protocol.packet.play.PostEventPacket;

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

    public void register(final @NonNull String service, final @NonNull Class<? extends Event>[] events) {
        for (Class<? extends Event> eventClazz : events) {
            bySubscribedServices
                    .computeIfAbsent(eventClazz, k -> new HashSet<>())
                    .add(service);
        }
    }

    public void unregister(final @NonNull String service) {
        for (Set<String> services : bySubscribedServices.values()) {
            services.remove(service);
        }
    }
}
