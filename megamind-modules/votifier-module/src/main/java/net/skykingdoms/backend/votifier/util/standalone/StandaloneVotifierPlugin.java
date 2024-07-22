package net.skykingdoms.backend.votifier.util.standalone;

import net.skykingdoms.backend.votifier.model.Vote;
import net.skykingdoms.backend.votifier.net.VotifierServerBootstrap;
import net.skykingdoms.backend.votifier.net.VotifierSession;
import net.skykingdoms.backend.votifier.platform.JavaUtilLogger;
import net.skykingdoms.backend.votifier.platform.LoggingAdapter;
import net.skykingdoms.backend.votifier.platform.VotifierPlugin;
import net.skykingdoms.backend.votifier.platform.scheduler.ScheduledExecutorServiceVotifierScheduler;
import net.skykingdoms.backend.votifier.platform.scheduler.VotifierScheduler;

import java.net.InetSocketAddress;
import java.security.Key;
import java.security.KeyPair;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class StandaloneVotifierPlugin implements VotifierPlugin {
    private final Map<String, Key> tokens;
    private final VoteReceiver receiver;
    private final KeyPair v1Key;
    private final InetSocketAddress bind;
    private final VotifierScheduler scheduler;
    private VotifierServerBootstrap bootstrap;

    public StandaloneVotifierPlugin(Map<String, Key> tokens, VoteReceiver receiver, KeyPair v1Key, InetSocketAddress bind) {
        this.receiver = receiver;
        this.bind = bind;
        this.tokens = Collections.unmodifiableMap(new HashMap<>(tokens));
        this.v1Key = v1Key;
        this.scheduler = new ScheduledExecutorServiceVotifierScheduler(Executors.newScheduledThreadPool(1));
    }

    public void start() {
        start(o -> {});
    }

    public void start(Consumer<Throwable> error) {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }
        this.bootstrap = new VotifierServerBootstrap(bind.getHostString(), bind.getPort(), this, v1Key == null);
        this.bootstrap.start(error);
    }

    @Override
    public Map<String, Key> getTokens() {
        return tokens;
    }

    @Override
    public KeyPair getProtocolV1Key() {
        return v1Key;
    }

    @Override
    public LoggingAdapter getPluginLogger() {
        return new JavaUtilLogger(Logger.getAnonymousLogger());
    }

    @Override
    public VotifierScheduler getScheduler() {
        return scheduler;
    }

    @Override
    public void onVoteReceived(Vote vote, VotifierSession.ProtocolVersion protocolVersion, String remoteAddress) throws Exception {
        receiver.onVote(vote);
    }

    @Override
    public void onError(Throwable throwable, boolean alreadyHandledVote, String remoteAddress) {
        receiver.onException(throwable);
    }
}
