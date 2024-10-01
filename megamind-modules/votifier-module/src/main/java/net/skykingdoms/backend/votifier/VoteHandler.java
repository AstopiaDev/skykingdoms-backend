package net.skykingdoms.backend.votifier;

import net.skykingdoms.backend.votifier.model.Vote;
import net.skykingdoms.backend.votifier.net.VotifierSession;

public interface VoteHandler {

    void onVoteReceived(Vote vote, VotifierSession.ProtocolVersion protocolVersion, String remoteAddress) throws Exception;

    default void onError(Throwable throwable, boolean voteAlreadyCompleted, String remoteAddress) {
        throw new RuntimeException("Unimplemented onError handler");
    }

}
