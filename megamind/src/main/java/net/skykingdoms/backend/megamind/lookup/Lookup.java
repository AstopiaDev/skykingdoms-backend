package net.skykingdoms.backend.megamind.lookup;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.protocol.packet.play.S20LookupResponse;
import net.skykingdoms.backend.protocol.type.lookup.LookupResponse;
import net.skykingdoms.backend.protocol.type.lookup.LookupTarget;

/**
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public final class Lookup {

    AbstractPlayerHolder server;
    LookupTarget[] targets;

    @NonFinal
    volatile LookupResponse queuedResponse;

    public Lookup addTargets(LookupTarget[] targets) {
        return new Lookup(server, LookupTarget.merge(targets, this.targets), queuedResponse);
    }

    public synchronized void flushQueuedResponse() {
        if (queuedResponse != null) {
            server.getChannel().sendPacket(new S20LookupResponse(queuedResponse));
            queuedResponse = null;
        }
    }

    public synchronized void queueResponse(LookupResponse response) {
        if (queuedResponse != null) {
            queuedResponse = LookupResponse.merge(queuedResponse, response);
        } else {
            queuedResponse = response;
        }
    }

}