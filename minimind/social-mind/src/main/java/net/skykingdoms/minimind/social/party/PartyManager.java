package net.skykingdoms.minimind.social.party;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.command.dependency.Dependency;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.minimind.social.party.event.PartyCreateEvent;
import net.skykingdoms.minimind.social.party.event.PartyDisbandEvent;
import net.skykingdoms.minimind.social.party.event.PartyLeaveEvent;
import net.skykingdoms.minimind.social.party.event.PartySettingUpdateEvent;

import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@Getter
public final class PartyManager {

    // ------------------------------------------------------------
    @Dependency
    public static final PartyManager INSTANCE = new PartyManager();
    // ------------------------------------------------------------

    /**
     * Party ID to Party instance map
     */
    Int2ObjectMap<Party> parties = new Int2ObjectArrayMap<>();

    /**
     * Restores all persistent parties from database
     */
    public void postBootstrap() {
/*        PartySqlHandler.INSTANCE.loadAllParties().whenComplete((_parties, throwable) -> {

            if (throwable != null) {
                Logging.IMP.stacktrace(throwable, "[PartyManager] Error raised on persistent party loading");
                return;
            }

            // Adding all restored party instances to list of active parties
            for (val party : _parties) {
                if (party.validate()) {
                    parties.put(party.getPartyId(), party);
                }
            }

            // removing old persistent records
            PartySqlHandler.INSTANCE.cleanUp();

            // Assuming that ALL players will reconnect in 15 seconds
            AsyncExecution.INSTANCE.runLater(() -> {
                for (val party : parties.values()) {
                    for (val member : party.getAllMembers()) {

                        // this player is offline, but reconnect time passed
                        if (!member.isOnline()) {
                            Logging.IMP.info("[PartyManager] Restored party #{} has offline gamer: {}", party.getPartyId(), member.getDisplayName());
                            party.addOfflineGamer(member.getGamerId());
                        }
                    }
                }
            }, 15, TimeUnit.SECONDS);

            Logging.IMP.info("[PartyManager] Restored in total {} parties with {} overall members",
                    parties.keySet().size(), parties.values().stream()
                            .map(party -> Arrays.stream(party.getPartyMembers().keySet().toIntArray()).sum())
                            .reduce(0, Integer::sum));
        }).join();*/

        AsyncExecution.INSTANCE.runTimer(() -> {
            for (val party : parties.values()) {
                val removedInvites = party.tickInvitations();
                for (val invite : removedInvites) {
                    val invitedGamer = Gamer.getOfflineGamer(invite.getInvitedId(), AbstractGamer::loadGroupsAndName);
                    val senderGamer = Gamer.getOfflineGamer(invite.getInviterId(), AbstractGamer::loadGroupsAndName);
                    if (invitedGamer.isOnline()) {
                        invitedGamer.sendLocalizedMessage("party_invite_expired",
                                "%player%", senderGamer.getDisplayName());
                    }

                    val senderParty = getParty(senderGamer.getGamerId());
                    if (senderParty == null) {
                        Logging.IMP.warn("[PartyManager] Ticked invite, but invert's guy party is null!! O_o");
                        continue;
                    }

                    // removing shit from party invite requests
                    senderParty.invalidateInvite(invitedGamer.getGamerId());

                    // notifying party members that request to someone expired
                    senderParty.broadcastParty("party_invite_expired_broadcast",
                            "%player%", invitedGamer.getDisplayName());
                }

                val removedOfflineNigger = party.tickOfflineMembers();
                removedOfflineNigger.forEach((IntConsumer) nigger -> removeMember(Gamer.getOfflineGamer(nigger, AbstractGamer::loadGroupsAndName), PartyRemoveReason.LONG_OFFLINE));

                if (!party.validate()) {
                    // avoiding CME by running this in separate thread
                    AsyncExecution.INSTANCE.run(() -> removePartyInstance(party));
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Returns a party that gamer is currently in
     */
    public Party getParty(final int gamerId) {
        return parties.values().stream()
                .filter(party -> party.getPartyMembers().keySet().contains(gamerId)
                        || party.getOfflineMembers().stream().anyMatch(offlinePartyMember -> offlinePartyMember.getGamerId() == gamerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Sends a party invite to some gamer
     */
    public boolean sendPartyInvite(final int senderId,
                                   final int targetId) {

        // target is already in party
        val _targetParty = getParty(targetId);
        if (_targetParty != null) {
            return false;
        }

        // we know tact sender != null, and he's online
        val senderGamer = Gamer.getGamer(senderId);

        val targetGamer = Gamer.getOfflineGamer(targetId, AbstractGamer::loadGroupsAndName);
        // this shouldn't happen, because we validate shit in command
        // but added just "in case"
        if (targetGamer == null || !targetGamer.isOnline()) {
            return false;
        }

        // if sender has no party, we create and register one instance
        var senderParty = getParty(senderId);
        if (senderParty == null) {
            senderParty = PartyFactory.bakeParty(senderId);
            senderGamer.setParty(senderParty);
            registerPartyInstance(senderParty);
        }

        // adding shit to party invite map
        senderParty.invitePlayer(senderId, targetId);
        senderParty.broadcastParty("party_invited_message_broadcast",
                "%inviter%", senderGamer.getDisplayName(),
                "%target%", targetGamer.getDisplayName());

        targetGamer.sendRawMessage(
                targetGamer.getLanguage().getMessage(targetGamer.isModern(), "party_invite_received",
                        "%playerDisplay%", senderGamer.getDisplayName(),
                        "%player%", senderGamer.getName())
        );

        return true;
    }

    /**
     * Accepts a party invite
     */
    public boolean acceptPartyInvite(final int acceptorId,
                                     final int inviterId) {
        val inviterParty = getParty(inviterId);
        if (inviterParty == null) {
            return false;
        }

        // we are sure that acceptor is online
        // because he just accepted the invite kekw
        val acceptorGamer = Gamer.getGamer(acceptorId);

        // Actually, an inviter might go offline
        // while invite was accepted, so he may be offline
        val inviterGamer = Gamer.getOfflineGamer(inviterId, AbstractGamer::loadGroupsAndName);

        if (inviterParty.getActiveInvites().stream().noneMatch(partyInvitation -> {
            return partyInvitation.getInvitedId() == acceptorId && partyInvitation.getInviterId() == inviterGamer.getGamerId();
        })) {
            acceptorGamer.sendMessage(ChatColor.RED + "You were not invited to this party!"); // todo localization
            return false;
        }

        // telling party that we accepted this dude
        inviterParty.addPartyMember(acceptorId, Party.PartyRole.MEMBER);
        acceptorGamer.setParty(inviterParty);

        inviterParty.invalidateInvite(acceptorId);

        inviterParty.broadcastParty("party_joined_message_broadcast",
                "%player%", acceptorGamer.getDisplayName());

        return true;
    }

    /**
     * Adds a party to registry
     *
     * @param party party instance
     */
    public void registerPartyInstance(final @NonNull Party party) {
        if (parties.get(party.getPartyId()) != null) {
            throw new IllegalArgumentException("Party with this ID already exists!");
        }

        // adding party to parties registry
        parties.put(party.getPartyId(), party);

        MegamindApp.INSTANCE.pluginManager().callEvent(new PartyCreateEvent(party));

        Logging.IMP.info("[PartyManager] Party #{} registered", party.getPartyId());
    }

    // basically we could create just a promote method
    // that automatically would calculate next role,
    // but we have only 3 of them, so i don't really care
    public boolean promote(final Gamer pendingModerator) {
        val party = getParty(pendingModerator.getGamerId());
        if (party == null) {
            return false;
        }

        if (party.getRole(pendingModerator.getGamerId()) == Party.PartyRole.MODERATOR) {
            return false; // already moderator
        }

        party.updateRole(pendingModerator.getGamerId(), Party.PartyRole.MODERATOR);
        party.broadcastParty("party_promoted_broadcast",
                "%player%", pendingModerator.getDisplayName());

        return true;
    }

    public boolean demote(final Gamer pendingModerator) {
        val party = getParty(pendingModerator.getGamerId());
        if (party == null) {
            return false;
        }

        if (party.getRole(pendingModerator.getGamerId()) == Party.PartyRole.MEMBER) {
            return false; // already member
        }

        party.updateRole(pendingModerator.getGamerId(), Party.PartyRole.MEMBER);
        party.broadcastParty("party_demoted_broadcast",
                "%player%", pendingModerator.getDisplayName());

        return true;
    }

    /**
     * Warps each party member to gamer's server
     */
    public void warpTo(final @NonNull Gamer gamer) {
        val party = getParty(gamer.getGamerId());
        if (party == null) {
            return;
        }

        val server = gamer.getCurrentServer();

        Logging.IMP.info(server.getName() + " is the name of the server btw" + server.canJoin() + " apparently thats the joinable");

        val needWarp = party.getAllMembers().stream()
                .filter(member -> member.getCurrentServer() != server)
                .filter(member -> member != gamer)
                .filter(Gamer::isOnline)
                .collect(Collectors.toSet());

        if (needWarp.size() > 0) {
            gamer.sendLocalizedMessage("party_warp_summoned_target",
                    "%players%", needWarp.stream()
                            .map(AbstractGamer::getDisplayName)
                            .collect(Collectors.joining(", ")));
        }

        // sending all niggers to target server
        needWarp.forEach(player -> {
            player.sendLocalizedMessage("party_warp_summoned",
                    "%player%", gamer.getDisplayName());
            player.sendTo(server);
        });
    }

    public boolean transferParty(final Gamer pendingLeader) {
        val party = getParty(pendingLeader.getGamerId());
        if (party == null) {
            return false;
        }

        val oldLeader = party.getLeaderGamer();
        if (pendingLeader == oldLeader) { // same leader
            return false;
        }

        // giving old leader moderator role
        // if he is still in party and exists as old leader
        if (oldLeader != null && oldLeader.getParty() == party) {
            party.updateRole(oldLeader.getGamerId(), Party.PartyRole.MODERATOR);
        }
        party.updateRole(pendingLeader.getGamerId(), Party.PartyRole.LEADER);


        // if new leader is nigger and cant host private games
/*        if (party.isSettingEnabled(Party.Setting.PRIVATE_GAMES)
                && !party.getLeaderGamer().getRanks().canApply(Group.GODLIKE)) {
            updatePartySetting(pendingLeader, Party.Setting.PRIVATE_GAMES, false);
        }*/

        // notifying niggers we updated leader
        if (oldLeader == null) {
            party.broadcastParty("party_transferred_broadcast",
                    "%player%", pendingLeader.getDisplayName());
        } else {
            // also mentions that previous leader is
            party.broadcastParty("party_transferred_full_broadcast",
                    "%player%", pendingLeader.getDisplayName(),
                    "%oldleader%", oldLeader.getDisplayName());
        }

        return true;
    }

    /**
     * Updates party setting
     *
     * @param updater   who calls update
     * @param setting   which setting we update
     * @param value     new setting value
     */
    public boolean updatePartySetting(final @NonNull Gamer updater,
                                      final Party.Setting setting,
                                      final boolean value) {
        val party = getParty(updater.getGamerId());
        if (party == null) {
            return false;
        }

        val oldValue = party.getSetting(setting);
        val updated = party.setSetting(setting, value);

        // notifying party members that some setting has been updated
        if (updated) {
            if (value) {
                party.broadcastParty("party_" + setting.name().toLowerCase().replace("_", "") + "_enabled_broadcast", "%player%", updater.getDisplayName());
            } else {
                party.broadcastParty("party_" + setting.name().toLowerCase().replace("_", "") + "_disabled_broadcast", "%player%", updater.getDisplayName());
            }
        }

        // if changes applied we call update event >w<
        if (updated) {
            MegamindApp.INSTANCE.pluginManager().callEvent(new PartySettingUpdateEvent(party,
                    setting,
                    oldValue,
                    value));
        }

        return updated;
    }

    /**
     * Removes a party from registry
     *
     * @param party     party instance
     */
    public void removePartyInstance(final @NonNull Party party) {
        if (parties.get(party.getPartyId()) == null) {
            return; // no throwable
        }

        // sending localized disbanded message
        if (party.getPartyMembers().size() <= 1) {
            party.broadcastParty("party_disbanded_empty_broadcast");

            MegamindApp.INSTANCE.pluginManager().callEvent(new PartyDisbandEvent(party, PartyDisbandEvent.DisbandReason.EVERYONE_LEFT));
        } else {
            party.broadcastParty("party_disbanded_broadcast");

            MegamindApp.INSTANCE.pluginManager().callEvent(new PartyDisbandEvent(party, PartyDisbandEvent.DisbandReason.LEADER_DISBANDED));
        }

        // updating all gamers with null party
        party.getAllMembers().forEach(gamer -> gamer.setParty(null));

        // removing from list of registered parties
        parties.remove(party.getPartyId());

        Logging.IMP.info("[PartyManager] Removed party #{} -> {}", party.getPartyId(), party.toString());
    }

    public void persistParties() {
        for (val party : parties.values()) {
            PartySqlHandler.INSTANCE.persistParty(party);
            Logging.IMP.info("[PartyManager] Persisted party #{}", party.getPartyId());
        }
    }

    public boolean removeMember(final @NonNull Gamer member,
                                final @NonNull PartyRemoveReason removeReason) {
        val party = getParty(member.getGamerId());
        if (party == null) {
            return false;
        }

        member.setParty(null);

        party.removePartyMember(member.getGamerId());

        MegamindApp.INSTANCE.pluginManager().callEvent(new PartyLeaveEvent(party, member));

        val valid = party.validate();

        // cocken ladder $$$$
        party.broadcastParty(
                removeReason == PartyRemoveReason.LEAVE
                        ? "party_player_left_broadcast"
                        : removeReason == PartyRemoveReason.LONG_OFFLINE
                        ? "party_player_kickoffline_broadcast"
                        : "party_player_removed_broadcast",
                "%player%", member.getDisplayName());

        // everyone left :(
        if (!valid) {
            removePartyInstance(party);
            return true;
        }

        // leader left, choosing new one
        if (party.getAllByRole(Party.PartyRole.LEADER).isEmpty() && valid) {
            Gamer newLeader;

            // shouldn't throw, actually
            if (party.getAllByRole(Party.PartyRole.MODERATOR).isEmpty()) {
                newLeader = party.getAllByRole(Party.PartyRole.MEMBER).stream().findAny().orElseThrow();
            } else {
                newLeader = party.getAllByRole(Party.PartyRole.MODERATOR).stream().findAny().orElseThrow();
            }

            // setting him as new leader
            party.broadcastParty("party_new_leader_selected",
                    "%player%", newLeader.getDisplayName());

            transferParty(newLeader);

        }

        return true;
    }

    // region handlers

    /**
     * Invoked when a gamer disconnects
     */
    public void handleDisconnect(final Gamer gamer) {
        val party = getParty(gamer.getGamerId());
        if (party == null) {
            return;
        }

        party.addOfflineGamer(gamer.getGamerId());

        party.broadcastParty("party_disconnected_broadcast",
                "%player%", gamer.getDisplayName());
    }

    /**
     * Invoked when player joins the server
     */
    public void handleJoin(final @NonNull Gamer gamer) {
        val party = getParty(gamer.getGamerId());
        if (party == null) {
            return;
        }

        party.invalidateOfflineGamer(gamer.getGamerId());
    }

    // endregion

}
