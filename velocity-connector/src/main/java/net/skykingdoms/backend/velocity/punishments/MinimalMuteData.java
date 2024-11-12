package net.skykingdoms.backend.velocity.punishments;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.time.Duration;
import net.skykingdoms.backend.base.util.time.DurationField;
import net.skykingdoms.backend.base.util.time.DurationFormatter;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Data
public final class MinimalMuteData {

    /**
     * Meta ID
     */
    int muteId;

    /**
     * Violator ID
     */
    int violatorId;

    /**
     * Mute reason
     */
    String reason;

    /**
     * Date when the mute is lifted.
     * It can not be permanent!
     */
    LocalDateTime expiration;

    public String getExpirationFormatted(final @NonNull Language language) {
        // TODO: rewrite this to support more taht just two localizations
        return Duration.format(LocalDateTime.now(), expiration, Duration.FormatParams.builder()
                .sourceUnit(TimeUnit.MILLISECONDS)
                .locale(language == Language.RUSSIAN ? DurationFormatter.RU : DurationFormatter.EN)
                .max(DurationField.DAY).min(DurationField.SEC).build());
    }

    /**
     * This method is called ONLY and ONLY in case coordinator
     * didn't send us remove meta packet.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiration);
    }

}
