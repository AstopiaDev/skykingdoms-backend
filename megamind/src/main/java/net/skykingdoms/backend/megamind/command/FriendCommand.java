package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.RelayExecutor;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.enums.Privacy;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.data.friends.FriendshipMeta;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.time.TimeUtil;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.friend.FriendManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;
import net.skykingdoms.backend.megamind.server.impl.GameServer;
import net.skykingdoms.backend.megamind.server.impl.LobbyServer;
import net.skykingdoms.backend.megamind.server.impl.PersistentServer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@WithAlias("f")
@WithAlias("friends")
@NamedCommand("friend")
public final class FriendCommand {

    @WithCondition("minArgs: 1")
    @WithAlias("invite")
    @SubcommandExecutor("add")
    @Executor
    public static void friendAdd(final CoreExecutionSession session) throws CommandTranslationException {
        val gamer = session.getGamer();
        val target = session.getOfflineGamer(0, $target -> {
            $target.loadSettings();
            $target.loadFriends();
        });

        if (target == session.getGamer()) {
            throw new CommandTranslationException("friend_add_self");
        }

        if (target.isFriend(gamer.getGamerId())) {
            throw new CommandTranslationException("friend_already_friends");
        }

        if (FriendManager.INSTANCE.getRequest(gamer.getGamerId(), target.getGamerId()) != null) {
            throw new CommandTranslationException("friend_already_sent_invitation");
        }

        val targetPrivacy = (Privacy) target.getEnumSetting(SettingType.FRIEND_REQUEST).getValue();
        if (targetPrivacy == Privacy.MAX && !gamer.isStaff()) {
            // only staff members allowed to send request
            throw new CommandTranslationException("friend_privacy_forbidden");
        } else if (targetPrivacy == Privacy.HIGH && (!gamer.isStaff())) {
            // only staff and guild members
            throw new CommandTranslationException("friend_privacy_forbidden");
        } // todo: write this when all privacy criteria are ready

        FriendManager.INSTANCE.sendFriendRequest(gamer.getGamerId(), target.getGamerId());
    }

    @SubcommandExecutor("accept")
    @WithCondition("minArgs: 1")
    public static void friendAccept(final CoreExecutionSession session) throws CommandTranslationException {
        // target might be offline
        val target = session.getOfflineGamer(0, CommonGamer::loadFriends);
        val gamer = session.getGamer();

        // no request from target to sender
        if (FriendManager.INSTANCE.getRequest(target.getGamerId(), gamer.getGamerId()) == null) {
            throw new CommandTranslationException("friend_request_absent");
        }

        FriendManager.INSTANCE.acceptFriendRequest(gamer.getGamerId(), target.getGamerId());
    }

    @SubcommandExecutor("deny")
    @WithCondition("minArgs: 1")
    public static void friendDeny(final CoreExecutionSession session) throws CommandTranslationException {
        // target might be offline
        val target = session.getOfflineGamer(0, CommonGamer::loadFriends);
        val gamer = session.getGamer();

        // no request from target to sender
        if (FriendManager.INSTANCE.getRequest(target.getGamerId(), gamer.getGamerId()) == null) {
            throw new CommandTranslationException("friend_request_absent");
        }

        FriendManager.INSTANCE.denyRequest(gamer.getGamerId(), target.getGamerId());
    }

    @SubcommandExecutor("remove")
    @WithCondition("minArgs: 1")
    public static void friendRemove(final CoreExecutionSession session) throws CommandTranslationException {
        // target might be offline
        val target = session.getOfflineGamer(0, CommonGamer::loadFriends);
        val gamer = session.getGamer();

        // they are not even friends
        if (!gamer.isFriend(target.getGamerId())) {
            throw new CommandTranslationException("friend_not_friends");
        }

        FriendManager.INSTANCE.removeFriend(gamer.getGamerId(), target.getGamerId());
    }

    @SubcommandExecutor("list")
    @RelayExecutor("fl")
    public static void friendList(final CoreExecutionSession session) throws CommandTranslationException {
        val gamer = session.getGamer();
        List<FriendshipMeta> friends = gamer.getFriends().stream().collect(Collectors.toList());

        // Separate and sort online and offline friends
        List<FriendshipMeta> onlineFriends = friends.stream()
                .filter(meta -> Gamer.getOfflineGamer(meta.getFriendIdFor(gamer.getGamerId()), $g -> {
                    $g.loadGroups();
                    $g.loadPlayerName();
                }).isOnline())
                .sorted((meta1, meta2) -> {
                    String name1 = Gamer.getOfflineGamer(meta1.getFriendIdFor(gamer.getGamerId()), $g -> {
                        $g.loadGroups();
                        $g.loadPlayerName();
                    }).getDisplayName();
                    String name2 = Gamer.getOfflineGamer(meta2.getFriendIdFor(gamer.getGamerId()), $g -> {
                        $g.loadGroups();
                        $g.loadPlayerName();
                    }).getDisplayName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());

        List<FriendshipMeta> offlineFriends = friends.stream()
                .filter(meta -> !Gamer.getOfflineGamer(meta.getFriendIdFor(gamer.getGamerId()), $g -> {
                    $g.loadGroups();
                    $g.loadPlayerName();
                }).isOnline())
                .sorted((meta1, meta2) -> {
                    String name1 = Gamer.getOfflineGamer(meta1.getFriendIdFor(gamer.getGamerId()), $g -> {
                        $g.loadGroups();
                        $g.loadPlayerName();
                    }).getDisplayName();
                    String name2 = Gamer.getOfflineGamer(meta2.getFriendIdFor(gamer.getGamerId()), $g -> {
                        $g.loadGroups();
                        $g.loadPlayerName();
                    }).getDisplayName();
                    return name1.compareToIgnoreCase(name2);
                })
                .collect(Collectors.toList());

        // Combine sorted lists
        List<FriendshipMeta> sortedFriends = new ArrayList<>();
        sortedFriends.addAll(onlineFriends);
        sortedFriends.addAll(offlineFriends);

        // Pagination
        int page = 1;
        try {
            if (session.getArgument(0) != null) {
                page = Integer.parseInt(session.getArgument(0));
            }
        } catch (Exception ignored) {
        }
        int pageSize = 10;
        int totalPages = (int) Math.ceil((double) sortedFriends.size() / pageSize);

        if (page < 1 || page > totalPages) {
            throw new CommandTranslationException("invalid_page_number");
        }

        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, sortedFriends.size());

        List<String> friendsList = sortedFriends.subList(startIndex, endIndex).stream()
                .filter(meta -> {
                    Gamer friend = Gamer.getOfflineGamer(meta.getFriendIdFor(gamer.getGamerId()), $g -> {
                        $g.loadGroups();
                        $g.loadPlayerName();
                    });

                    if (friend == null) {
                        return false;
                    }

                    if (friend.isVanished()) {
                        return gamer.isStaff();
                    }

                    return true;
                })
                .map(meta -> {
                    Gamer friend = Gamer.getOfflineGamer(meta.getFriendIdFor(gamer.getGamerId()), $g -> {
                        $g.loadGroups();
                        $g.loadPlayerName();
                    });

                    if (friend == null) {
                        return "N/A";
                    }

                    String displayName = friend.getColoredName();

                    if (!friend.isOnline()) {
                        return displayName + "§c is currently offline.";
                    }

                    AbstractPlayerHolder holder = friend.getCurrentServer();
                    if (holder instanceof GameServer) {
                        GameServer gameServer = (GameServer) holder;
                        return displayName + "§a is currently playing " + gameServer.getGameType().getHumanName();
                    } else if (holder instanceof LobbyServer) {
                        LobbyServer lobbyServer = (LobbyServer) holder;
                        return displayName + "§a is currently in " + lobbyServer.getGameType().getHumanName() + " Lobby";
                    } else if (holder instanceof PersistentServer) {
                        PersistentServer persistentServer = (PersistentServer) holder;
                        return displayName + "§a is currently in " + persistentServer.getGameMode().getHumanName();
                    }
                    return "#" + meta.getMetaId() + " " + displayName + " - " + TimeUtil.getDateString((Language) gamer.getLanguage(), LocalDate.from(meta.getBeginning()));
                })
                .collect(Collectors.toList());

        String response = String.format("§b§lYour Friend List §8- §fPage %d of %d\n", page, totalPages) + String.join("\n", friendsList);
        session.sendMessage(response);
    }

}