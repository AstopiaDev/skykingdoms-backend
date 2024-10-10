package net.skykingdoms.backend.base.permission;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.backend.base.mysql.Database;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public enum PermissionSqlHandler {
    INSTANCE;

    private static final String LOAD_PLAYER_PERMISSIONS
            = "SELECT `Permission`, `Expires` FROM `UserPermissions` WHERE `GamerId` = ? AND `GameMode` = ?;";

    private static final String ADD_PLAYER_PERMISSION
            = "INSERT INTO `UserPermissions` (`GamerId`, `Permission`, `GameMode`, `Expires`) VALUES (?, ?, ?, ?)";

    private static final String REMOVE_PLAYER_PERMISSION
            = "DELETE FROM `UserPermissions` WHERE `Id` = ?";

    private static final String LOAD_GROUP_PERMISSIONS
            = "SELECT `Permission`, `Expires` FROM `GroupPermissions` WHERE `GamerId` = ? AND `GameMode` = ?;";

    private static final String ADD_GROUP_PERMISSION
            = "INSERT INTO `GroupPermissions` (`GamerId`, `Permission`, `GameMode`, `Expires`) VALUES (?, ?, ?, ?)";

    private static final String REMOVE_GROUP_PERMISSION
            = "DELETE FROM `GroupPermissions` WHERE `Id` = ?";

    // ---------------------------------------------------

    Database userDatabase = new MySQL(DatabaseCredentials.MAIN.withDatabase("UserPermissions"));
    Database groupDatabase = new MySQL(DatabaseCredentials.MAIN.withDatabase("GroupPermissions"));

    private static final Map<Group, Map<String, Boolean>> groupPermissions = new HashMap<>();
    private static final Cache<Integer, Map<String, Boolean>> userPermissions = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build();


}

