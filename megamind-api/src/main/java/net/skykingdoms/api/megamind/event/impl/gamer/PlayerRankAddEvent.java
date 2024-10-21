package net.skykingdoms.api.megamind.event.impl.gamer;

import lombok.NonNull;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

/**
 * @author Vihara
 */
public final class PlayerRankAddEvent extends RankChangedEvent {

    public PlayerRankAddEvent(final @NonNull IMegamindGamer gamer,
                              final @NonNull IUserRank rank) {
        super(gamer, rank);
    }
}
