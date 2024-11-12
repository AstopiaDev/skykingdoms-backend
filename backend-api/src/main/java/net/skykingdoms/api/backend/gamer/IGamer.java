package net.skykingdoms.api.backend.gamer;

import net.skykingdoms.api.backend.language.ILanguage;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.backend.permission.UserRankContainer;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.UUID;

public interface IGamer extends ICommandSender {

    /**
     * @return unique ID of this player in database
     */
    int getGamerId();

    /**
     * @return mojang's player UUID
     */
    @NotNull UUID getPlayerUniqueId();

    /**
     * @return raw mojang's player name
     */
    @NotNull String getName();

    /**
     * @return player's display name (prefixed with rank)
     */
    @NotNull String getDisplayName();

    /**
     * @return player's selected language
     */
    @NotNull ILanguage getLanguage();

    /**
     * @return player's ranks
     */
    UserRankContainer<? extends IUserRank> getRanks();

    /**
     * @return player's address
     */
    InetAddress getAddress();

    /**
     * @return player's protocol
     */
    int getProtocol();

    /**
     * @return player's auth status?
     */
    boolean isPremium();

    /**
     * @return player's login status
     */
    boolean isLoggedIn();

    default void sendRawMessage(String message) {
        sendMessage(message);
    }

    default @NotNull String getLowerName() {
        return getName().toLowerCase();
    }

}
