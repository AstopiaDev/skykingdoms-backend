package net.skykingdoms.backend.base.security;

public interface IHandshakeSecurityTokenProvider {

    /**
     * @return security token that encapsulates into BungeeCord handshake
     */
    String getSecurityToken();

    /**
     * Silly method
     */
    default boolean isInitialized() {
        return getSecurityToken() != null;
    }

}
