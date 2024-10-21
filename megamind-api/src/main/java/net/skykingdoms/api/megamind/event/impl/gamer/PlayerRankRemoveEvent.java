package net.skykingdoms.api.megamind.event.impl.gamer;

import lombok.NonNull;
import net.skykingdoms.api.backend.permission.IUserRank;
import net.skykingdoms.api.megamind.gamer.IMegamindGamer;

public final class PlayerRankRemoveEvent extends RankChangedEvent {

    public PlayerRankRemoveEvent(final @NonNull IMegamindGamer gamer,
                                 final @NonNull IUserRank rank) {
        super(gamer, rank);
    }
}
