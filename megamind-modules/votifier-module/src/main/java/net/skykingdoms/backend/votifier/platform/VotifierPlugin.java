package net.skykingdoms.backend.votifier.platform;

import io.netty.util.AttributeKey;
import net.skykingdoms.backend.votifier.VoteHandler;
import net.skykingdoms.backend.votifier.platform.scheduler.VotifierScheduler;

import java.security.Key;
import java.security.KeyPair;
import java.util.Map;

public interface VotifierPlugin extends VoteHandler {
    AttributeKey<VotifierPlugin> KEY = AttributeKey.valueOf("votifier_plugin");

    Map<String, Key> getTokens();

    KeyPair getProtocolV1Key();

    LoggingAdapter getPluginLogger();

    VotifierScheduler getScheduler();

    default boolean isDebug() {
        return false;
    }
}
