package net.skykingdoms.minimind.party;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.mysql.DatabaseCredentials;
import net.skykingdoms.backend.base.mysql.impl.MySQL;
import net.skykingdoms.backend.base.util.string.StringUtil;
import net.skykingdoms.backend.megamind.data.party.Party;
import net.skykingdoms.backend.megamind.data.party.PartyFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * This SQL handler is used to persist party data and restore it
 * in case if coordinator restarts.
 *
 * @author Vihara
 */
public final class PartySqlHandler {

    // ------------------------------------------------------------------
    public static final PartySqlHandler INSTANCE = new PartySqlHandler();
    // ------------------------------------------------------------------

    /**
     * Loads all persistent parties from database
     */
    String LOAD_ALL_PARTIES = "SELECT * FROM `SysInternal`.`Parties`;";

    /**
     * Saves basic party information to database
     */
    String PERSIST_PARTY    = "INSERT INTO `SysInternal`.`Parties` " +
            "(`Id`, `PartyLeader`, `PartyModerators`, `PartyMembers`, `KickedGamers`, `PartySettings`) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    /**
     * Removes party persistent record from database
     */
    String DELETE_PARTY = "DELETE FROM `SysInternal`.`Parties` WHERE `Id` = ?;";

    /**
     * Removes every persistent party record
     */
    String CLEANUP_PARTIES = "DELETE FROM `SysInternal`.`Parties`;";

    MySQL database = new MySQL(DatabaseCredentials.MAIN.withDatabase("SysInternal"));

    public CompletableFuture<Set<Party>> loadAllParties() {
        return database.executeQuery(LOAD_ALL_PARTIES).thenApply(rows -> {
            val parties = new HashSet<Party>();
            if (rows.size() == 0) {
                return parties;
            }

            for (val row : rows) {
                parties.add(PartyFactory.bakeRestoredParty(
                        row.getRequiredInt("PartyLeader"),
                        row.getInt("PartySettings").orElse(0),
                        StringUtil.deserializeIntArray(row.getString("KickedGamers").orElse("[]")),
                        StringUtil.deserializeIntArray(row.getString("PartyMembers").orElse("[]")),
                        StringUtil.deserializeIntArray(row.getString("PartyModerators").orElse("[]")))
                );
            }

            return parties;
        });
    }

    /**
     * Persists party in database
     * in order to restore it after
     * coordinator reboot.
     */
    public void persistParty(final @NonNull Party party) {
        database.executeUpdate(PERSIST_PARTY,
                party.getPartyId(),
                party.getLeaderGamer().getGamerId(),
                Arrays.toString(party.getAllByRole(Party.PartyRole.MODERATOR, true).stream().mapToInt(AbstractGamer::getGamerId).toArray()),
                Arrays.toString(party.getAllByRole(Party.PartyRole.MEMBER, true).stream().mapToInt(AbstractGamer::getGamerId).toArray()),
                Arrays.toString(party.getKickedGamers().toIntArray()),
                party.getSettings());
    }

    public void removeParty(final int partyId) {
        database.executeVoidUpdate(DELETE_PARTY, partyId);
    }

    public void cleanUp() {
        database.executeVoidUpdate(CLEANUP_PARTIES);
    }
}
