package net.skykingdoms.api.megamind.event;


import net.skykingdoms.api.protocol.packet.serialize.Serializable;

/**
 * Dummy class which all callable events must extend.
 */
public abstract class Event implements Serializable<Event> {

    public Event() {
    }

    /**
     * Method called after this event has been dispatched to all handlers.
     */
    public void postCall() {

    }
}
