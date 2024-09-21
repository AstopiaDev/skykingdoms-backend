package net.skykingdoms.minimind.social.party;

import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.language.LanguageContext;
import net.skykingdoms.backend.base.util.string.ObjectLinkedBuf;
import net.skykingdoms.minimind.social.SocialGamer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
@Getter
public final class Party {

    /**
     * 60 seconds = invite lifetime
     */
    public static final Long INVITE_LIFETIME = 1000L * 60;

    /**
     * 5 minutes for every disconnected player to reconnect
     * until they get auto-kicked
     */
    public static final Long OFFLINE_LIFETIME = 5000L * 60;

    // ------------------------------------------------------

    /**
     * Unique party ID
     */
    final int partyId;

    /**
     * Party members map
     * Gamer ID - Role (0 - MEMBER, 1 - MODERATOR)
     */
    final Int2IntMap partyMembers = new Int2IntArrayMap();

    /**
     * List of kicked gamer ID's
     */
    final IntSet kickedGamers = new IntArraySet();

    /**
     * Invited Gamer ID to Epoch-timestamp of invitation
     */
    final ObjectList<PartyInvitation> activeInvites = new ObjectArrayList<>();

    /**
     * List containing disconnected party members
     */
    final ObjectList<OfflinePartyMember> offlineMembers = new ObjectArrayList<>();

    /**
     * Bitmask for all party settings
     */
    @Setter int settings;

    public boolean isSettingEnabled(final @NonNull Setting setting) {
        return setting.contains(settings);
    }

    public boolean setSetting(final @NonNull Setting setting,
                              boolean value) {

        // setting is already set to this value
        if (setting.contains(settings) && value) return false;
        if (!setting.contains(settings) && !value) return false;

        settings = Setting.toMask(Arrays.stream(Setting.VALUES)
                .filter(_set -> _set == setting && value)
                .toArray(Setting[]::new));

        return true;
    }

    public Party(final int partyId,
                 final int leaderId) {
        this.partyId = partyId;

        // all settings disabled by default
        this.settings = 0;

        // adding leader to members list with LEADER role
        partyMembers.put(leaderId, PartyRole.LEADER.ordinal());
    }

    /**
     * Returns a Gamer object based on leader ID
     */
    public @Nullable SocialGamer getLeaderGamer() {
        // shouldn't throw, actually :nerd:
        // we are sure that leader exists in database, otherwise
        // how the fuck the party was created
        return getAllByRole(PartyRole.LEADER).stream().findFirst().orElse(null);
    }

    /**
     * Returns collection with Gamer instances of each member
     * This includes LEADER, MODERATORS and other MEMBERS
     */
    @SuppressWarnings("SimplifyStreamApiCallChains") // different schemas
    public Collection<SocialGamer> getAllMembers() {
        val ret = new HashSet<SocialGamer>();

        // Adding each member instance to ret list
        partyMembers.keySet().stream().forEach(memberId -> ret.add(SocialGamer.getOfflineGamer(memberId, AbstractGamer::loadGroupsAndName)));

        return ret;
    }

    public void addPartyMember(final int gamerId,
                               final @NonNull PartyRole partyRole) {
        partyMembers.put(gamerId, partyRole.ordinal());
    }

    public void removePartyMember(final int gamerId) {
        partyMembers.remove(gamerId);
        offlineMembers.removeIf(offlinePartyMember -> offlinePartyMember.getGamerId() == gamerId);
    }
    
    public PartyRole getRole(final int gamerId) {
        return PartyRole.byId(partyMembers.get(gamerId));
    }

    public Collection<SocialGamer> getAllByRole(final @NonNull PartyRole role) {
        return getAllByRole(role, false);
    }

    public Collection<SocialGamer> getAllByRole(final @NonNull PartyRole role,
                                          final boolean onlyOnline) {
        val ret = new HashSet<SocialGamer>();
        for (val partyMember : partyMembers.int2IntEntrySet()) {
            if (role.ordinal() != partyMember.getIntValue()) {
                continue;
            }

            // this gui is offline
            if (onlyOnline &&
                    offlineMembers.stream()
                    .anyMatch(offlinePartyMember -> offlinePartyMember.getGamerId() == partyMember.getIntKey())) {
                continue;
            }

            ret.add(SocialGamer.getOfflineGamer(partyMember.getIntKey(), AbstractGamer::loadGroupsAndName));
        }

        return ret;
    }

    public void updateRole(final int gamerId,
                           final PartyRole newRole) {
        partyMembers.remove(gamerId);
        partyMembers.put(gamerId, newRole.ordinal());
    }

    /**
     * Adds a player to invites map
     */
    public void invitePlayer(int senderId,
                             int targetId) {
        activeInvites.add(new PartyInvitation(senderId, targetId, System.currentTimeMillis()));
    }

    /**
     * Removing active invite from invites map
     */
    public void invalidateInvite(final int targetId) {
        activeInvites.removeIf(invite -> invite.getInvitedId() == targetId);
    }

    public void addOfflineGamer(final int gamerId) {
        // we already have this member in offline list
        if (offlineMembers.stream().anyMatch(member -> member.getGamerId() == gamerId)) {
            return;
        }

        offlineMembers.add(new OfflinePartyMember(gamerId, System.currentTimeMillis()));
    }

    public void invalidateOfflineGamer(final int gamerId) {
        offlineMembers.removeIf(offlinePartyMember -> offlinePartyMember.gamerId == gamerId);
    }

    /**
     * Broadcasts localized party message
     */
    public void broadcastParty(final @NonNull String localizationKey,
                               final Object... args) {

        val buffer = new ObjectLinkedBuf(args);
        broadcastParty(new LanguageContext(localizationKey, lang -> buffer));
    }

    /**
     * Broadcasts party message by LanguageContext
     */
    public void broadcastParty(final @NonNull LanguageContext ctx) {
        for (val gamer : getAllMembers()) {
            if (!gamer.isOnline()) {
                continue;
            }

            gamer.sendMessage(ctx.format(gamer.getLanguage()));
        }
    }

    public boolean getSetting(final @NonNull Setting setting) {
        return setting.contains(settings);
    }

    /**
     * Ticks every second to remove outdated invitations
     *
     * @return set of expired invites
     */
    public Collection<PartyInvitation> tickInvitations() {
        val removed = new HashSet<PartyInvitation>();

        for (val invite : activeInvites) {
            if (System.currentTimeMillis() - invite.sendTimestamp > INVITE_LIFETIME) {
                removed.add(invite);
            }
        }

        return removed;
    }

    /**
     * Ticks every second to remove all offline
     * players that disconnected for too long
     *
     * @return set of gamer IDs disconnected for too long
     */
    public IntSet tickOfflineMembers() {
        val removed = new IntArraySet();

        for (val offlinePartyMember : offlineMembers) {
            if (System.currentTimeMillis() - offlinePartyMember.disconnectTimestamp > INVITE_LIFETIME) {
                removed.add(offlinePartyMember.gamerId);
            }
        }

        return removed;
    }

    public boolean isLeader(final @NonNull SocialGamer gamer) {
        return getLeaderGamer().getGamerId() == gamer.getGamerId();
    }

    /**
     * Returns if party can exist
     */
    public boolean validate() {
        return partyMembers.size() > 1 || activeInvites.size() > 0;
    }

    // region impl

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Party)) return false;

        Party party = (Party) o;

        return partyId == party.partyId;
    }

    @Override
    public int hashCode() {
        return partyId;
    }

    // endregion

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor
    @ToString
    @Getter
    public static final class OfflinePartyMember {

        /**
         * ID of offline gamer
         */
        int gamerId;

        /**
         * Epoch MS timestamp when the player disconnected
         */
        long disconnectTimestamp;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor
    @ToString
    @Getter
    public static final class PartyInvitation {

        /**
         * Who sent invitation
         */
        int inviterId;

        /**
         * A player ID who is invited
         */
        int invitedId;

        /**
         * Epoch MS timestamp of invitation
         */
        long sendTimestamp;

    }

    public enum PartyRole {

        MEMBER, MODERATOR, LEADER;

        public static PartyRole byId(final int id) {
            return values()[id];
        }
    }

    public enum Setting {

        ALL_INVITE, PRIVATE_GAMES, MUTE, PUBLIC_STATUS;

        public static final Setting[] VALUES = values();

        /**
         * Converts setting enum to bitmask
         */
        public int toMask() {
            return 1 << ordinal();
        }

        public boolean contains(final int mask) {
            return (toMask() & mask) == toMask();
        }

        public static Setting[] getSettings(final int mask) {
            return Arrays.stream(VALUES).filter(v -> v.contains(mask)).toArray(Setting[]::new);
        }

        public static int toMask(final Setting @NotNull [] arr) {
            return Arrays.stream(arr)
                    .map(Setting::toMask)
                    .reduce(0, (accumulator, b) -> accumulator | b);
        }

        public static Setting valueOf(final @NonNull String string,
                                      final @Nullable Setting fallback) {
            return Arrays.stream(VALUES).filter(s -> s.name().equalsIgnoreCase(string)
                        || s.name().replace("_", "").equalsIgnoreCase(string))
                    .findFirst()
                    .orElse(fallback);
        }
    }
}
