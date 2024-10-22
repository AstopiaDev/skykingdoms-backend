package net.skykingdoms.backend.megamind.data.friend;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.friends.FriendsSqlHandler;
import net.skykingdoms.backend.base.data.friends.FriendshipMeta;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.friend.event.FriendAddEvent;
import net.skykingdoms.backend.megamind.data.friend.event.FriendRemoveEvent;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.packet.play.S23FriendshipAction;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Friends manager.
 * Handles persistent set of FriendshipMetadatas
 * @author Vihara
 */
@Getter
public final class FriendManager {

    // --------------------------------------------------------------
    public static final FriendManager INSTANCE = new FriendManager();
    // --------------------------------------------------------------

    Collection<FriendshipMeta> friendshipMetas = CollectionUtil.createConcurrentSet();

    /**
     * Invoked once. Loads all shit from database here.
     */
    public void postBootstrap() {
        /*friendshipMetas.addAll(FriendsSqlHandler.INSTANCE.getFriendships());*/
        Logging.IMP.info("[FriendManager] Loaded {} friendship metas", friendshipMetas.size());
    }

    /**
     * Returns list of accepted friendship metas
     */
    public Collection<FriendshipMeta> getByPlayer(final int gamerId) {
        return friendshipMetas.stream()
                // filter metas that are already accepted
                .filter(meta -> meta.getBeginning() != null)
                // filter metas by player
                .filter(meta -> meta.getFirstGamerId() == gamerId || meta.getSecondGamerId() == gamerId)
                .collect(Collectors.toSet());
    }

    /**
     * Returns list of friend requests, directed to {@code gamerId}
     */
    public Collection<FriendshipMeta> getRequests(final int gamerId) {
        return friendshipMetas.stream()
                // filter metas that are not accepted yet
                .filter(meta -> meta.getBeginning() == null)
                // filter metas by player
                .filter(meta -> meta.getFirstGamerId() == gamerId || meta.getSecondGamerId() == gamerId)
                .collect(Collectors.toSet());
    }

    public FriendshipMeta getFriendship(final int firstGamerId,
                                        final int secondGamerId) {
        return getByPlayer(firstGamerId).stream()
                .filter(meta -> meta.getFriendIdFor(secondGamerId) == firstGamerId)
                .filter(meta -> meta.getFriendIdFor(firstGamerId) == secondGamerId)// idk why this retarded should be here
                .findFirst()
                .orElse(null);
    }

    public FriendshipMeta getById(final int metaId) {
        return friendshipMetas.stream()
                .filter(meta -> meta.getMetaId() == metaId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get request that is directed to target
     */
    public FriendshipMeta getRequest(final int senderId,
                                     final int targetId) {
        return getRequests(targetId).stream()
                .filter(request -> request.getFirstGamerId() == senderId && request.getSecondGamerId() == targetId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates a FriendshipMeta and pushes it to set.
     */
    public FriendshipMeta sendFriendRequest(final int senderId,
                                            final int targetId) {
        // they are already friends
        val _friendship = getFriendship(senderId, targetId);
        if (_friendship != null) {
            return _friendship;
        }

        // we already have a request send from sender to target
        val existing = getRequest(senderId, targetId);
        if (existing != null) {
            return existing;
        }

        // target already sent us a request
        val reverseExisting = getRequest(targetId, senderId);
        if (reverseExisting != null)  {
            acceptFriendRequest(senderId, targetId);
            return reverseExisting;
        }

        // sender is online, because he sent le request
        val senderGamer = Gamer.getGamer(senderId);

        // but target might be offline, so we don't send him a message
        val targetGamer = Gamer.getOfflineGamer(targetId, CommonGamer::load);
        if (targetGamer.isOnline()) {
            targetGamer.sendRawMessage(
                    targetGamer.getLanguage().getMessage(targetGamer.isModern(), "friend_request_received",
                            "%playerDisplay%", senderGamer.getColoredName(),
                            "%player%", senderGamer.getName()
                    )
            );
        }

        senderGamer.sendLocalizedMessage(senderGamer.isModern(), "friend_request_sent", "%player%", targetGamer.getColoredName());

        val friendshipMeta = FriendsSqlHandler.INSTANCE.createFriendshipMeta(senderId, targetId);
        friendshipMetas.add(friendshipMeta);
        return friendshipMeta;
    }

    /**
     * Accepts friend request.
     * @param receiverId a guy who has this friend invitation
     */
    public boolean acceptFriendRequest(final int receiverId,
                                       final int senderId) {
        val request = getRequest(senderId, receiverId);
        if (request == null) { // no request found by this criteria
            return false;
        }

        // setting date of friendship beginning to now
        request.setBeginning(LocalDateTime.now());

        val packet = new S23FriendshipAction(
                request.getMetaId(),
                request.getFirstGamerId(),
                request.getSecondGamerId(),
                S23FriendshipAction.Action.ADD_FRIEND,
                request.getBeginning());

        val receiverGamer = Gamer.getOfflineGamer(receiverId, g -> {
            /*g.loadGroups();*/
            g.loadPlayerName();
            g.loadFriends();
        });

        val senderGamer = Gamer.getOfflineGamer(senderId, g -> {
            /*g.loadGroups();*/
            g.loadPlayerName();
            g.loadFriends();
        });

        if (receiverGamer == null || senderGamer == null) {
            Logging.IMP.error("[FriendManager/AFR] One of gamers is NULL! F: {} @ S: {}",
                    receiverGamer, senderGamer);
            return false;
        }

        if (receiverGamer.isOnline()) {
            receiverGamer.sendLocalizedMessage(receiverGamer.isModern(), "friend_request_accepted",
                    "%player%", senderGamer.getColoredName());
            receiverGamer.sendToServer(packet);
            receiverGamer.sendToProxy(packet);
        }

        if (senderGamer.isOnline()) {
            senderGamer.sendLocalizedMessage(senderGamer.isModern(), "friend_request_accepted",
                    "%player%", receiverGamer.getColoredName());
            senderGamer.sendToServer(packet);
            senderGamer.sendToProxy(packet);
        }

        // adding shit to gamer data class
        receiverGamer.addFriend(request);
        senderGamer.addFriend(request);

        FriendsSqlHandler.INSTANCE.updateFriendshipMeta(request);

        MegamindApp.INSTANCE.pluginManager().callEvent(new FriendAddEvent(senderGamer, receiverGamer));

        return true;
    }

    public boolean removeFriend(final int removerId,
                                final int friendId) {
        val meta = getByPlayer(removerId).stream()
                .filter($meta -> $meta.getFriendIdFor(removerId) == friendId)
                .findFirst()
                .orElse(null);

        if (meta == null) { // not even friends
            return false;
        }

        val packet = new S23FriendshipAction(
                meta.getMetaId(),
                meta.getFirstGamerId(),
                meta.getSecondGamerId(),
                S23FriendshipAction.Action.REMOVE_FRIEND,
                null);

        val firstGamer = Gamer.getOfflineGamer(removerId, g -> {
            /*g.loadGroups();*/
            g.loadPlayerName();
            g.loadFriends();
        });

        val secondGamer = Gamer.getOfflineGamer(friendId, g -> {
            /*g.loadGroups();*/
            g.loadPlayerName();
            g.loadFriends();
        });

        if (firstGamer == null || secondGamer == null) {
            Logging.IMP.error("[FriendManager/RF] One of gamers is NULL! F: {} @ S: {}",
                    firstGamer, secondGamer);
            return false;
        }

        if (firstGamer.isOnline()) {
            // you removed %player%
            firstGamer.sendLocalizedMessage(firstGamer.isModern(), "friend_removed", "%player%", secondGamer.getColoredName());
            firstGamer.sendToServer(packet);
            firstGamer.sendToProxy(packet);
        }

        if (secondGamer.isOnline()) {
            // %player% removed you
            secondGamer.sendLocalizedMessage(secondGamer.isModern(), "friend_removed_by", "%player%", firstGamer.getColoredName());
            secondGamer.sendToServer(packet);
            secondGamer.sendToProxy(packet);
        }

        // removing shit from gamer data classes
        secondGamer.removeFriend(firstGamer.getGamerId());

        firstGamer.removeFriend(secondGamer.getGamerId());

        friendshipMetas.remove(meta);

        FriendsSqlHandler.INSTANCE.removeFriendshipMeta(meta);

        MegamindApp.INSTANCE.pluginManager().callEvent(new FriendRemoveEvent(firstGamer, secondGamer));

        return true;
    }

    public boolean denyRequest(final int denierId,
                               final int senderId) {

        val request = getRequest(senderId, denierId);
        if (request == null) { // no request found by this criteria
            return false;
        }

        val denierGamer = Gamer.getOfflineGamer(denierId, g -> {
            /*g.loadGroups();*/
            g.loadPlayerName();
            g.loadFriends();
        });

        val senderGamer = Gamer.getOfflineGamer(senderId, g -> {
            /*g.loadGroups();*/
            g.loadPlayerName();
            g.loadFriends();
        });

        if (denierGamer == null || senderGamer == null) {
            Logging.IMP.error("[FriendManager/DFR] One of gamers is NULL! F: {} @ S: {}",
                    denierGamer, senderGamer);
            return false;
        }

        if (denierGamer.isOnline()) {
            denierGamer.sendLocalizedMessage(denierGamer.isModern(), "friend_request_denied",
                    "%player%", senderGamer.getColoredName());
        }

        if (senderGamer.isOnline()) {
            senderGamer.sendLocalizedMessage(senderGamer.isModern(), "friend_request_denied_by",
                    "%player%", denierGamer.getColoredName());
        }

        friendshipMetas.remove(request);

        FriendsSqlHandler.INSTANCE.removeFriendshipMeta(request);

        return true;
    }

    // region --------------------- HANDLERS --------------------------

    public void handlePlayerQuit(final @NonNull Gamer gamer) {

    }

    // endregion
}