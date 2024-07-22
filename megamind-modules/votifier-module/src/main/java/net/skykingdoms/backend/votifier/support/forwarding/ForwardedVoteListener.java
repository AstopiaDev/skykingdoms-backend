package net.skykingdoms.backend.votifier.support.forwarding;

import net.skykingdoms.backend.votifier.model.Vote;

/**
 * Represents a method to call when a vote is forwarded through the {@link ForwardingVoteSink}.
 */
public interface ForwardedVoteListener {

    /**
     * Called whenever a vote is forwarded from a {@link ForwardingVoteSink}
     *
     * @param v Vote that was forwarded
     */
    void onForward(Vote v);

}
