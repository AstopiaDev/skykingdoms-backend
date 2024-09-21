package net.skykingdoms.backend.base.gamer;

import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.permission.UserRanks;
import net.skykingdoms.backend.api.protocol.type.PropertyType;
import org.jetbrains.annotations.NotNull;

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
    @NotNull Language getLanguage();
    UserRanks getRanks();

    int getProperty(@NotNull PropertyType key);

    /**
     * Добавить значение к существующей проперти
     */
    void addProperty(PropertyType key, int value);

    /**
     * Установить значение для проперти
     */
    void setProperty(PropertyType key, int value);

    void removeProperty(PropertyType key);

    default void sendRawMessage(String message) {
        sendMessage(message);
    }

    default @NotNull String getLowerName() {
        return getName().toLowerCase();
    }

}
