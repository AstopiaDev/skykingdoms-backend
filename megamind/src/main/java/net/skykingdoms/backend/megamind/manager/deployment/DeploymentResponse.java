package net.skykingdoms.backend.megamind.manager.deployment;

import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

/**
 * 
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public enum DeploymentResponse {

    /**
     * If node sent denial response
     */
    DENIED_BY_NODE,

    /**
     * If request packet failed to reach node
     */
    TRANSACTION_FAILED,

    /**
     * If no instance booted up until timeout reached
     */
    DEPLOYMENT_TIMEOUT,

    /**
     * If instance booted up successfully
     */
    SUCCESSFUL_BOOT

    ;

    /**
     * A instance transaction ID that we need for testflowyyyy thing
     */
    @Accessors(fluent = true, chain = true)
    UUID transactionId;

    public static DeploymentResponse successful(final @NonNull UUID server) {
        val ret = SUCCESSFUL_BOOT;
        ret.transactionId = server;
        return ret;
    }

}
