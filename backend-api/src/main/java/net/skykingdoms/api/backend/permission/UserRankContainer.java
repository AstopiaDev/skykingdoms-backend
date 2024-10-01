package net.skykingdoms.api.backend.permission;

import lombok.NonNull;

import java.util.AbstractSet;
import java.util.Collection;

public abstract class UserRankContainer<T extends IUserRank> extends AbstractSet<T> {

    public abstract Collection<? extends T> getRanks(final @NonNull PermissionType type);
    /*boolean contains(final @NonNull Collection<? extends IUserRank> ranks,
                            final @NonNull Group group);*/
    public abstract Collection<? extends T> getExpiredRanks();
    public abstract Collection<? extends T> getAllRanks();
    public abstract IUserRank getPriorityRank();
    public abstract IUserRank getPriorityRank(final @NonNull PermissionType permission);
    public abstract boolean canApply(Group group);
}
