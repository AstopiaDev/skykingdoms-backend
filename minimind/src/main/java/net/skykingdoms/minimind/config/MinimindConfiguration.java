package net.skykingdoms.minimind.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@AllArgsConstructor
@EqualsAndHashCode
@Data
public class MinimindConfiguration {
    /**
     * Address of remote coordinator service instance
     */
    String remoteCoordinatorHost;
    int remoteCoordinatorPort;

    /**
     * The secret needed for communication in the backend
     */
    String secret;
}
