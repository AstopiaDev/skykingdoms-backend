package net.skykingdoms.backend.base.data.security;

/**
 * @author Vihara
 */
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
