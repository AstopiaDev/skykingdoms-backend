package net.skykingdoms.backend.base.data.sql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Getter
public final class GameMap {

    /**
     * Map system name
     * eg: bw_lighthouse
     */
    String name;

    /**
     * Map human name
     * eg: Lighthouse
     */
    String humanName;

    /**
     * Byte array of map ZIP file
     */
    byte[] data;

}
