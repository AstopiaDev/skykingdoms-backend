package net.skykingdoms.backend.base.data.currencies;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.GameType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vihara
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class GameCurrenciesHolder {

    /**
     * ID of gamer who's holding this balance
     */
    int gamerId;

    /**
     * Here we store name -> amount
     */
    Map<String, Integer> balances = new HashMap<>();

    // region coins

    public int getCoins(final @NonNull GameType gameType) {
        return balances.getOrDefault(_generateCoinsName(gameType), 0);
    }

    public void setCoins(final @NonNull GameType gameType,
                        final int amount) {
        if (balances.containsKey(_generateCoinsName(gameType))) {
            balances.replace(_generateCoinsName(gameType), amount);
        } else {
            balances.put(_generateCoinsName(gameType), amount);
        }
    }

    public void addCoins(final @NonNull GameType gameType,
                        final int amount) {
        val value = getCoins(gameType);
        setCoins(gameType, value + amount);
    }

    public void takeCoins(final @NonNull GameType gameType,
                          final int amount) {
        val value = getCoins(gameType);
        setCoins(gameType, value - amount);
    }

    private String _generateCoinsName(final @NonNull GameType gameType) {
        return gameType.getCode() + "_coins";
    }

    // endregion

    // region SW tokens

  /*  public int getTokens() {
        return balances.getOrDefault("sw_tokens", 0);
    }

    public void setTokens(final int amount) {
        if (balances.containsKey("sw_tokens")) {
            balances.replace("sw_tokens", amount);
        } else {
            balances.put("sw_tokens", amount);
        }
    }

    public void addTokens(final int amount) {
        val value = getTokens();
        setTokens(value + amount);
    }

    public void takeTokens(final int amount) {
        val value = getTokens();
        setTokens(value - amount);
    }*/
}
