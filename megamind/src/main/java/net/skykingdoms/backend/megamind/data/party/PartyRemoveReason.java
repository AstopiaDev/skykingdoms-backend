package net.skykingdoms.backend.megamind.data.party;

/**
 * @author Vihara
 */
public enum PartyRemoveReason {

    /**
     * Player was offline for too long
     */
    LONG_OFFLINE,

    /**
     * Player left the party manually
     */
    LEAVE,

    /**
     * Moderator or leader removed this gamer from party
     */
    REMOVED

}
