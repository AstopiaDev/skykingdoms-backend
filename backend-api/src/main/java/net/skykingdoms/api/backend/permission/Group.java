package net.skykingdoms.api.backend.permission;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * All groups registered in network
 * @author Vihara
 */
@AllArgsConstructor
@ToString
@Getter
public enum Group {

    /**
     * Default group
     */
    DEFAULT(1, 0, "§cN/A", "§e", "§e", PermissionType.DEFAULT),

    /**
     * Purchasable groups
     */
    VIP(10, 10, "§bVip", "§bVip §8» §b", "§b", PermissionType.DEFAULT),
    DONATOR(15, 15, "§2Donator", "§2Donator §8» §2", "§2", PermissionType.DEFAULT),
    HERO(20, 20, "§cHero", "§cHero §8» §c", "§c", PermissionType.DEFAULT),
    ELITE(25, 25, "§aElite", "§aElite §8» §a", "§a", PermissionType.DEFAULT),
    LEGEND(26, 25, "§dLegend", "§dLegend §8» §d", "§d", PermissionType.DEFAULT),
    DEMIGOD(27, 25, "§9Demigod", "§9Demigod §8» §9", "§9", PermissionType.DEFAULT),
    GOD(28, 25, "§bGod", "§bGod §8» §b", "§b", PermissionType.DEFAULT),
    TITAN(29, 25, "§3Titan", "§3Titan §8» §3", "§3", PermissionType.DEFAULT),
    INFINITY(30, 25, "§5Infinity", "§5Infinity §8» §5", "§5", PermissionType.DEFAULT),
    ZEUS(31, 25, "§6Zeus", "§6Zeus §8» §6", "§6", PermissionType.DEFAULT),
/*    *//**
     * Subscriptions
     * MVP++ will have its own "+" colors
     *//*
    LEGEND(30, 30, "§6MVP§c++", "§6[MVP§c++§6] ", "§6", PermissionType.SUBSCRIPTION),*/

    /**
     * Media
     */
    YOUTUBER(40, 35, "§cYouTube", "§4§lY§f§lT §8» §f", "§f", PermissionType.MEDIA),
    YOUTUBERPLUS(41, 35, "§cYouTube+", "§4§lY§f§lT§4§l+ §8» §f", "§f", PermissionType.MEDIA),
    YOUTUBERPLUSPLUS(42, 35, "§cYouTube++", "§4§lY§f§lT§4§l++ §8» §f", "§f", PermissionType.MEDIA),

    /**
     * Other
     */
    TRANSLATOR(110, 5, "§7TRANSLATOR", "§7[TRANSLATOR] ", "§7", PermissionType.OTHER),

    /**
     * People trying for the benefit of the server - staff
     */
    BUILDER(50, 40, "§2§lBuilder", "§2§lBuilder §8» §2", "§2", PermissionType.STAFF),
    JR_HELPER(63, 43, "§a§lJr. Helper", "§a§lJr. Helper §8» §a", "§a", PermissionType.STAFF),
    HELPER(65, 45, "§a§lHelper", "§a§lHelper §8» §a", "§a", PermissionType.STAFF),
    MODERATOR(70, 50, "§6§lMod", "§6§lMod §8» §6", "§6", PermissionType.STAFF),
    JR_DEV(72, 52, "§5§lJr. Dev", "§5§lJr. Dev §8» §5", "§5", PermissionType.STAFF),
    DEV(75, 55, "§5§lDev", "§5§lDev §8» §5", "§5", PermissionType.STAFF),
    ADMIN(80, 60, "§c§lAdmin", "§c§lAdmin §8» §c", "§c", PermissionType.STAFF),

    /**
     * seniorProjectAdministration
     */
    OWNER(100, 100, "§4Owner", "§4§lOwner §8» §4", "§4", PermissionType.STAFF);

    private final int id, level;

    private final String name, prefix, suffix, prefixColor;

    private final PermissionType permissions;

    Group(int id, int level, String name, String prefix, String suffix, PermissionType permissions) {
        this(id, level, name, prefix, suffix, prefix.substring(0, 2), permissions);
    }

    /**
     * getGroupDisplayName
     *
     * @return - groupDisplayName
     */
    public String getDisplayName() {
        return name;
    }

    public static final Group[] VALUES = Group.values();

    public boolean isStaff() {
        return permissions == PermissionType.STAFF && level >= HELPER.getLevel();
    }

    public static Group getById(PermissionType type, int id) {
        return _getById(id)
                .filter(group -> type == null || group.getPermissions() == type)
                .findAny().orElse(type == PermissionType.DEFAULT || type == null ? DEFAULT : null);
    }

    public static Group getById(int id) {
        return _getById(id).findAny().orElse(DEFAULT);
    }

    /**
     * getAGroupByItsIDIfThereIsNoSuchGroup
     * theDefaultGroupWillReturn DEFAULT
     *
     * @param id - groupId
     * @return - groupByThisID
     */
    private static Stream<Group> _getById(int id) {
        return Arrays.stream(VALUES).filter(group -> group.getId() == id);
    }

    /**
     * Get a group by its name. If there is no such group,
     * theDefaultGroupWillReturn (DEFAULT)
     *
     * @param legacyName - legacyGroupName
     * @return - groupByThisName
     */
    public static Group fromLegacyGroup(@NonNull String legacyName) {
        return Arrays.stream(VALUES)
                .filter(group -> group.name().equalsIgnoreCase(legacyName))
                .findFirst().orElse(DEFAULT);
    }

}
