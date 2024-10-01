package net.skykingdoms.backend.base.permission;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SortedSetMultimap;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.permission.PermissionType;
import net.skykingdoms.api.backend.permission.UserRankContainer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
public final class UserRanks extends UserRankContainer<UserRank> {

    public static UserRanks CONSOLE = new UserRanks(Collections.singleton(UserRank.OWNER));

    // -----------------------------------------------------------------------

    private final SortedSetMultimap<PermissionType, UserRank> ranks
            = Multimaps.newSortedSetMultimap(new HashMap<>(), TreeSet::new);

    public UserRanks(final @NonNull Collection<UserRank> ranks) {
        for (UserRank rank : ranks) {
            this.ranks.put(rank.getType(), rank);
        }
    }

    @Override
    public boolean add(final @NonNull UserRank rank) {
        return ranks.get(rank.getType()).add(rank);
    }

    @Override
    public boolean remove(Object o) {
        if (!(o instanceof UserRank)) {
            return false;
        }

        val rank = (UserRank) o;

        return rank.getId() >= 0 && ranks.remove(rank.getType(), rank);
    }

    @Override
    public void clear() {
        ranks.clear();
    }

    @Override
    public @NotNull Iterator<UserRank> iterator() {
        return ranks.values().iterator();
    }

    @Override
    public Collection<UserRank> getRanks(final @NonNull PermissionType type) {
        return Collections.unmodifiableCollection(ranks.get(type));
    }

    public boolean contains(final @NonNull Collection<UserRank> ranks,
                            final @NonNull Group group) {
        return ranks.stream().anyMatch(rank -> rank.matches(group));
    }

    public Collection<UserRank> getExpiredRanks() {
        return ranks.values().stream().filter(UserRank::isExpired)
                .collect(Collectors.toList());
    }
    public Collection<UserRank> getAllRanks() {
        return new ArrayList<>(ranks.values());
    }

    public UserRank getPriorityRank() {
        PermissionType permission = ranks.isEmpty()
                ? PermissionType.DEFAULT :
                Collections.max(ranks.keySet());

        return getPriorityRank(permission);
    }

    public UserRank getPriorityRank(final @NonNull PermissionType permission) {
        SortedSet<UserRank> ranks = this.ranks.get(permission);

        return ranks.isEmpty() ?
                (permission == PermissionType.DEFAULT ? UserRank.DEFAULT : null) :
                ranks.first();
    }

    @Override
    public int size() {
        return ranks.size();
    }

    public boolean hasPermission(final @NonNull PermissionType type) {
        return ranks.containsKey(type);
    }

    public boolean canApply(final @NonNull Group other) {
        return ranks.values().stream().anyMatch(rank -> rank.canApply(other));
    }
}
