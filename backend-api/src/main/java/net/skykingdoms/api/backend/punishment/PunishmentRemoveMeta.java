package net.skykingdoms.api.backend.punishment;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Basically contains information on WHO and WHY removed certain punishment.
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public final class PunishmentRemoveMeta {

    /**
     * ID of this meta
     */
    int metaId;

    /**
     * ID of player who removed the punishment
     * 0 IF CONSOLE
     */
    int removerId;

    /**
     * Punishment removal reason
     */
    String reason;

    /**
     * Lift timestamp
     */
    LocalDateTime date;

}
