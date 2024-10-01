package net.skykingdoms.api.backend.permission;

import lombok.NonNull;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public interface IUserRank {
    long getExpires(final @NonNull TimeUnit unit);
    boolean canApply(@NonNull Group group);
    boolean matches(@NonNull Group group);
    LocalDateTime getExpiringTime();
    PermissionType getType();
    String getDisplayName();
    boolean isExpired();
    Group getGroup();
    int getLevel();
    int getId();
}
