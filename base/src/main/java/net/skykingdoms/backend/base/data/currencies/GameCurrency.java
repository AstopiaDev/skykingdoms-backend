package net.skykingdoms.backend.base.data.currencies;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.util.word.CommonWords;
import net.skykingdoms.backend.base.util.word.Noun;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public enum GameCurrency {

    /**
     * !!! LINKED TO SW !!
     * Works only for SkyClash game type
     */
    SC_TOKENS(CommonWords.SPIRIT), // fixme

    /**
     * Coins, depending on gametype (not global)
     */
    COINS(CommonWords.COIN),

    /**
     * Catpixel gold
     */
    GOLD(CommonWords.GOLD);

    public final int getId() {
        return ordinal() + 1;
    }

    Noun word;

}
