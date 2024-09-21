package net.skykingdoms.backend.megamind.command.condition;

/**
 * @author Vihara
 */
public final class CoreCommandConditions {

    private CoreCommandConditions() {
        throw new UnsupportedOperationException();
    }

/*
    @ConditionProvider(value = "notMuted", priority = -100)
    public static void notMuted(final CoreExecutionSession session, String meta) throws CommandMessageException {
        if (session.isConsole()) {
            return;
        }

        val gamer = session.getGamer();

        val mute = PunishmentManager.INSTANCE.getActivePunishment(gamer.getGamerId(), PunishmentType.MUTE);

        if (mute == null) {
            return;
        }

        val language = gamer.getLanguage();

        throw new CommandMessageException(PunishmentManager.INSTANCE.getPunishmentReasonFormatted(mute, gamer.getLanguage()));
    }
*/

/*    @ConditionProvider(value = "partyLeaderOnly", priority = 15)
    public static void partyLeaderOnly(CoreExecutionSession session, String meta) throws CommandTranslationException {
        val senderParty = (Party) session.getProvidedObject(0);

        if (senderParty == null || !senderParty.isLeader(session.getGamer())) {
            throw new CommandTranslationException("party_not_leader");
        }
    }

    @ConditionProvider(value = "withPartyOnly", priority = 10)
    public static void withPartyOnly(CoreExecutionSession session, String meta) throws CommandTranslationException {
        Party senderParty = session.getProvidedObject(0);

        if (senderParty == null) {
            throw new CommandTranslationException("party_without_party");
        }
    }
    @ConditionProvider(value = "withoutPartyOnly", priority = 10)
    public static void withoutPartyOnly(CoreExecutionSession session, String meta) throws CommandTranslationException {
        Party senderParty = session.getProvidedObject(0);

        if (senderParty != null) {
            throw new CommandTranslationException("party_with_party");
        }
    }
    @ConditionProvider(value = "partyModerationOnly", priority = 10)
    public static void partyModerationOnly(CoreExecutionSession session, String meta) throws CommandTranslationException {
        Party senderParty = session.getProvidedObject(0);

        if (senderParty == null || (!senderParty.isLeader(session.getGamer())
                && !senderParty.getAllByRole(Party.PartyRole.MODERATOR, false).contains(session.getGamer()))) {
            throw new CommandTranslationException("party_not_moderation");
        }
    }*/
}