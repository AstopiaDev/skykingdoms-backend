package net.skykingdoms.backend.votifier.util.standalone;

import net.skykingdoms.backend.votifier.model.Vote;

public interface VoteReceiver {
    void onVote(Vote vote) throws Exception;
    void onException(Throwable throwable);
}
