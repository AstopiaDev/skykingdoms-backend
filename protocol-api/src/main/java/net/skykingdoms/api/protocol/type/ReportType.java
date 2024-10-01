package net.skykingdoms.api.protocol.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum ReportType {

    /**
     * Chat-related shit
     */
    CHAT_GLOBAL,
    CHAT_PARTY,
    CHAT_CLUB,
    CHAT_DIRECT,

    /**
     * WatchCat related shit
     */
    CHEATING,

    /**
     * Character-related shit
     */
    BAD_NAME,
    BAD_SKIN_CAPE,

    /**
     * Boosting stats (PapaMawr mode)
     */
    STATS_BOOSTING,

    /**
     * Guild-related shit
     */
    CLUB_NAME,
    CLUB_TAG

    ;

    public String getLocalizationKey() {
        return "report_book_" + name().toLowerCase();
    }

}
