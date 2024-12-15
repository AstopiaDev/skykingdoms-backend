package net.skykingdoms.backend.messagechannel.type;

import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.HashSet;

public class DirectMessageChannel extends MessageChannel {
    public DirectMessageChannel(HashSet<Gamer> gamers) {
        super(gamers);
    }

    @Override
    public void sendMessage(Gamer gamer, String message) {

    }

    @Override
    public void addGamer(Gamer gamer) {

    }

    @Override
    public void removeGamer(Gamer gamer) {

    }
}
