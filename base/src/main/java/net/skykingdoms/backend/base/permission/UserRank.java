package net.skykingdoms.backend.base.permission;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.backend.permission.PermissionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class UserRank implements Comparable<UserRank>, IUserRank {

    public static UserRank DEFAULT = new UserRank(-1, Group.DEFAULT, null);
    public static UserRank OWNER = new UserRank(-1, Group.OWNER, null);

    // -----------------------------------------------------------------

    @Getter
    private final int id;
    @Getter
    private final Group group;
    private final LocalDateTime expires;

    public UserRank(final int id,
                    final @NonNull Group group,
                    final int expires) {
        this(id, group, expires == -1 ? null : Instant.ofEpochSecond(expires).atZone(ZoneId.systemDefault())
                .toLocalDateTime());
    }

    public UserRank(final int id,
                    final @NonNull Group group,
                    final @Nullable LocalDateTime expires) {
        this.id = id;
        this.group = group;
        this.expires = expires;
    }

    public boolean isExpired() {
        return expires != null && expires.isBefore(LocalDateTime.now());
    }

    public long getExpires(final @NonNull TimeUnit unit) {
        return expires == null
                ? -1
                : unit.convert(expires.atZone(ZoneId.systemDefault()).toEpochSecond(), TimeUnit.SECONDS);
    }

    public PermissionType getType() {
        return group.getPermissions();
    }

    public String getDisplayName() {
        return group.getDisplayName();
    }

    public int getLevel() {
        return group.getLevel();
    }

    public LocalDateTime getExpiringTime() {
        return expires;
    }

    public boolean canApply(@NonNull Group group) {
        return this.group.getLevel() >= group.getLevel();
    }

    public boolean matches(@NonNull Group group) {
        return this.group == group;
    }

    @Override
    public String toString() {
        return "UserRank{" +
                "group=" + group +
                ", expires=" + expires +
                '}';
    }

    @Override
    public int compareTo(@NotNull UserRank o) {
        return Integer.compare(o.group.getLevel(), group.getLevel());
    }
}
