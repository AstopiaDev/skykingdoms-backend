package net.skykingdoms.api.megamind.event;

/**
 * Events that implement this indicates that they may be cancelled and thus
 * prevented from happening.
 */
public interface Cancellable {

    /**
     * Get whether this event is cancelled.
     *
     * @return the cancelled state of this event
     */
    boolean isCancelled();

    /**
     * Sets the cancelled state of this event.
     *
     * @param cancel the state to set
     */
    void setCancelled(boolean cancel);

}
