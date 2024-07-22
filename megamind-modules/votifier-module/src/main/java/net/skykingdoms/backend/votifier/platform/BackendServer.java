package net.skykingdoms.backend.votifier.platform;

public interface BackendServer {
    String getName();

    boolean sendPluginMessage(String channel, byte[] data);
}
