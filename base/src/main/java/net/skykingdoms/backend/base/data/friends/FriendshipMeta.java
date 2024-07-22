package net.skykingdoms.backend.base.data.friends;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

/**
 * Class corresponds to holding minimal friendship data
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public final class FriendshipMeta {

    /**
     * Unique ID of this meta in database
     */
    int metaId;

    /**
     * Player ONE gamer id
     */
    int firstGamerId;

    /**
     * Player TWO gamer id
     */
    int secondGamerId;

    /**
     * Date when friend request was accepted
     */
    @EqualsAndHashCode.Exclude LocalDateTime beginning;

    /**
     * This retarded method saves us a bit of
     */
    public int getFriendIdFor(final int gamerId) {
        return firstGamerId == gamerId ? secondGamerId : firstGamerId;
    }

    /**
     * Also code cleaner
     */
    public boolean isFriendship(final int firstId,
                                final int secondId) {
        return (firstGamerId == firstId || secondGamerId == firstId)
                && (firstGamerId == secondId || secondGamerId == secondId);
    }
}
