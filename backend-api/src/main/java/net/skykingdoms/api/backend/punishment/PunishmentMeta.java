package net.skykingdoms.api.backend.punishment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Basic data-class represents any punishment data container
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public final class PunishmentMeta {

    /**
     * ID of this punishment in database
     */
    int metaId;

    /**
     * Punishment reason
     */
    String reason;

    /**
     * ID of player who is punished
     */
    int violatorId;

    /**
     * ID of player who executed the punishment.
     * <p>
     * 0 IF CONSOLE
     */
    int executorId;

    /**
     * Date of when this punishment was executed
     */
    LocalDateTime executionDate;

    /**
     * Date of punishment expiration
     * <p>
     * NULL if permanent
     */
    LocalDateTime expirationTime;

    /*
     * The server the player was punished
     * on!
     */
    String orignatedServer;

    /**
     * Punishment type
     */
    PunishmentType punishmentType;

    /**
     * Indicates if punishment is lifted
     */
    boolean lifted;

    /**
     * ID of lift removal meta
     * -1 IF NOT LIFTED
     */
    int liftMeta;

    public boolean isPermanent() {
        return expirationTime == null;
    }

    public boolean isExpired() {
        return expirationTime != null && expirationTime.isBefore(LocalDateTime.now());
    }

    public boolean isWatchCatBan() {
        return reason.contains("WATCHCAT CHEAT DETECTION");
    }

}
