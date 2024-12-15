package net.skykingdoms.backend.messagechannel.type;

import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.HashSet;

public interface IMessageChannel {
    void sendMessage(Gamer gamer, String message);
    void addGamer(Gamer gamer);
    void removeGamer(Gamer gamer);
    HashSet<Gamer> getGamers();

    default void processMessage(Gamer gamer, String message) {

    }
}
