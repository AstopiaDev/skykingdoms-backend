package net.skykingdoms.backend.megamind.manager.testflow;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;

/**
 * Data-class representing necessary information for testflow deployment
 * 
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Data
public final class TestFlowData {

    /**
     * Name of testflow
     */
    String name;

    /**
     * Server scale type, game type and game mode (if present)
     */
    ScalableDataImpl serverData;

    /**
     * Is debug mode enabled
     */
    boolean debug;

    /**
     * Port for jdwp agent
     */
    int debugPort;

}
