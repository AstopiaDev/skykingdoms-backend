package net.skykingdoms.backend.base.data.security;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
@Getter
public abstract class AbstractHandshakeSecurityManager implements IHandshakeSecurityTokenProvider {

    /**
     * Security token
     */
    String securityToken;

    public void updateToken(final @NonNull String securityToken) {
        this.securityToken = securityToken;
    }

    public boolean validate(final @NonNull String otherSecurityToken) {
        return securityToken.equals(otherSecurityToken);
    }

}
