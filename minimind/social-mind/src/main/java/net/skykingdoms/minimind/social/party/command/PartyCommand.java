package net.skykingdoms.minimind.social.party.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.RelayExecutor;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.execution.PrepareExecution;
import net.skykingdoms.api.backend.command.execution.SubcommandExecutor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.api.backend.permission.Group;
import net.skykingdoms.api.backend.setting.SettingType;
import net.skykingdoms.api.backend.setting.enums.Privacy;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.gamer.CommonGamer;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.minimind.command.MinimindExecutionSession;
import net.skykingdoms.minimind.social.SocialGamer;
import net.skykingdoms.minimind.social.party.Party;
import net.skykingdoms.minimind.social.party.PartyManager;
import net.skykingdoms.minimind.social.party.PartyRemoveReason;

import java.util.stream.Collectors;

/*
 @author Vihara
*/


@WithAlias("p")
@NamedCommand("party")
public final class PartyCommand {

    @PrepareExecution
    public static void prepare(MinimindExecutionSession session) {
        if (!session.isConsole()) {
            SocialGamer gamer = (SocialGamer) session.getGamer();
            Party party = gamer.getParty();

            session.provideObject(0, party);
        }
    }

    /*
         Basically sends invitation to arg gamer
     */

    @WithCondition("disableConsole")
    @Tabcomplete("[playerName]")
    @WithAlias("invite")
    @WithCondition("group: admin")
    @Executor
    public static void execute(final MinimindExecutionSession session) throws CommandTranslationException {
        if (session.getArgumentCount() == 0) {
            session.sendLocalizedMessage("party_command_help");
            return;
        }

        val gamer = (SocialGamer) session.getGamer();
        val target = (SocialGamer) session.getOfflineGamer(0, g -> {
            g.loadSettings();
            g.loadGroupsAndName();
        });

        if (!target.isOnline()) {
            throw new CommandTranslationException("party_invite_offline");
        }

        val targetPrivacy = (Privacy) target.getEnumSetting(SettingType.PARTY_INVITE_PRIVACY).getValue();
        if (targetPrivacy == Privacy.MAX && !gamer.isStaff()) {
            // todo
            throw new CommandTranslationException("party_invite_forbidden");
        } else if (targetPrivacy == Privacy.HIGH && (!gamer.isStaff() && !gamer.isFriend(target.getGamerId()))) {
            // todo
            throw new CommandTranslationException("party_invite_forbidden");
        } else if (targetPrivacy == Privacy.MEDIUM) {
            // todo
        }

        val senderParty = PartyManager.INSTANCE.getParty(gamer.getGamerId());
        if (senderParty != null && !senderParty.isSettingEnabled(Party.Setting.ALL_INVITE) &&
                (senderParty.getRole(gamer.getGamerId()) != Party.PartyRole.MODERATOR && senderParty.getRole(gamer.getGamerId()) != Party.PartyRole.LEADER)) {
            throw new CommandTranslationException("party_invite_forbidden_settings");
        }

        PartyManager.INSTANCE.sendPartyInvite(gamer.getGamerId(), target.getGamerId());
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("partyLeaderOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("disband")
    public static void disbandCommand(final MinimindExecutionSession session) {
        val gamer = (SocialGamer) session.getGamer();
        val party = gamer.getParty();

        PartyManager.INSTANCE.removePartyInstance(party);
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @SubcommandExecutor("list")
    @WithCondition("group: admin")
    @RelayExecutor("pl")
    public static void listCommand(final MinimindExecutionSession session) {
        val gamer = (SocialGamer) session.getGamer();
        val party = gamer.getParty();
        val lang = gamer.getLanguage();

        val sb = new StringBuilder();

        sb.append(lang.getMessage(session.getGamer().isModern(), "party_message_header")).append("\n");
        sb.append(lang.getMessage(session.getGamer().isModern(),"party_command_list_base",
                "%count%", party.getPartyMembers().keySet().size(),
                "%leader%", party.getLeaderGamer().getDisplayName() + (party.getLeaderGamer().isOnline() ? ChatColor.GREEN : ChatColor.RED) + " ●"));

        val mods = party.getAllByRole(Party.PartyRole.MODERATOR);
        if (!mods.isEmpty()) {
            sb.append("\n");
            sb.append(lang.getMessage(session.getGamer().isModern(),"party_command_list_mod_append",
                    "%moderators%", mods.stream()
                            .map(g -> g.getDisplayName() + (g.isOnline() ? ChatColor.GREEN : ChatColor.RED) + " ●")
                            .collect(Collectors.joining(ChatColor.WHITE + " "))));
        }

        val mems = party.getAllByRole(Party.PartyRole.MEMBER);
        if (!mems.isEmpty()) {
            sb.append("\n");
            sb.append(lang.getMessage(session.getGamer().isModern(),"party_command_list_mem_append",
                    "%members%", mems.stream()
                            .map(g -> g.getDisplayName() + (g.isOnline() ? ChatColor.GREEN : ChatColor.RED) + " ●")
                            .collect(Collectors.joining(ChatColor.WHITE + " "))));
        }

        sb.append(lang.getMessage(session.getGamer().isModern(),"party_message_header"));

        session.sendMessage(sb.toString());
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("leave")
    public static void leaveCommand(final MinimindExecutionSession session) throws CommandMessageException {
        val gamer = (SocialGamer) session.getGamer();
        if(!PartyManager.INSTANCE.removeMember(gamer, PartyRemoveReason.LEAVE)) {
            throw new CommandMessageException("Lol chzh......");
        }
    }

    @WithCondition("withoutPartyOnly")
    @WithCondition("disableConsole")
    @WithCondition("minArgs: 1")
    @Tabcomplete("[playerName]")
    @WithCondition("group: admin")
    @SubcommandExecutor("accept")
    public static void acceptCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        PartyManager.INSTANCE.acceptPartyInvite(session.getGamer().getGamerId(), session.getOfflineGamer(0, CommonGamer::loadGroupsAndName).getGamerId());
    }

    @WithCondition("disableConsole")
    @WithCondition("partyModerationOnly") // this applies to party MODS and LEADER
    @WithCondition("group: admin")
    @SubcommandExecutor("mute")
    public static void muteCommand(final MinimindExecutionSession session) {
        val gamer = (SocialGamer) session.getGamer();
        PartyManager.INSTANCE.updatePartySetting(gamer, Party.Setting.MUTE, !gamer.getParty().getSetting(Party.Setting.MUTE));
    }

    @WithCondition("withPartyOnly")
    @WithCondition("disableConsole")
    @WithCondition("partyLeaderOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("private")
    public static void privateGamesCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();

        if (!session.getRanks().canApply(Group.ZEUS) && !session.getRanks().canApply(Group.YOUTUBER)) {
            throw new CommandTranslationException("party_private_noperms");
        }


        PartyManager.INSTANCE.updatePartySetting(gamer, Party.Setting.PRIVATE_GAMES, !gamer.getParty().getSetting(Party.Setting.PRIVATE_GAMES));
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("partyLeaderOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("warp")
    public static void warpCommand(final MinimindExecutionSession session) {
        val gamer = (SocialGamer) session.getGamer();
        PartyManager.INSTANCE.warpTo(gamer);
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("partyLeaderOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("kickoffline")
    public static void kickOfflineCommand(final MinimindExecutionSession session) {
        val gamer = (SocialGamer) session.getGamer();
        val offlinePartyMembers = gamer.getParty();

        for (val offline : offlinePartyMembers.getOfflineMembers()) {
            PartyManager.INSTANCE.removeMember(SocialGamer.getOfflineGamer(offline.getGamerId()), PartyRemoveReason.LONG_OFFLINE);
        }

        val size = offlinePartyMembers.getOfflineMembers().size();

        if (size > 0) {
            session.sendLocalizedMessage("party_kickoffline_message",
                    "%amount%", size);
        }
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("partyModerationOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("kick")
    public static void kickCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();
        val target = (SocialGamer) session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);

        if (PartyManager.INSTANCE.getParty(target.getGamerId()) != gamer.getParty()) {

        }
        PartyManager.INSTANCE.removeMember(target, PartyRemoveReason.REMOVED);
    }

    @WithCondition("minArgs: 1")
    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("partyLeaderOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("promote")
    public static void promoteCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();
        val target = (SocialGamer) session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);

        if (gamer == target) {
            throw new CommandTranslationException("party_promote_self");
        }

        val party = gamer.getParty();
        if (party != PartyManager.INSTANCE.getParty(target.getGamerId())) {
            return;
        }

        // target's current role in party
        val targetRole = party.getRole(target.getGamerId());

        // transferring party to this faggot
        if (targetRole == Party.PartyRole.MODERATOR) {
            PartyManager.INSTANCE.transferParty(target);
            return;
        }

        if (targetRole == Party.PartyRole.MEMBER) {
            PartyManager.INSTANCE.promote(target);
        }
    }

    @WithCondition("minArgs: 1")
    @WithCondition("disableConsole")
    @WithCondition("partyLeaderOnly")
    @WithCondition("withPartyOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("transfer")
    public static void transferCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();
        val target = (SocialGamer) session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);
        if (gamer == target) {
            throw new CommandTranslationException("party_transfer_self");
        }

        val party = gamer.getParty();
        if (party != PartyManager.INSTANCE.getParty(target.getGamerId())) {
            return;
        }

        PartyManager.INSTANCE.transferParty(target);
    }

    @WithCondition("minArgs: 1")
    @WithCondition("disableConsole")
    @WithCondition("partyLeaderOnly")
    @WithCondition("withPartyOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("demote")
    public static void demoteCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();
        val target = (SocialGamer) session.getOfflineGamer(0, CommonGamer::loadGroupsAndName);
        if (gamer == target) {
            throw new CommandTranslationException("party_demote_self");
        }

        val party = gamer.getParty();

        // not gamer.getParty()
        if (party != PartyManager.INSTANCE.getParty(target.getGamerId())) {
            return;
        }

        // target's current role in party
        val targetRole = party.getRole(target.getGamerId());

        // target is not a moderator
        if (targetRole != Party.PartyRole.MODERATOR) {
            return;
        }

        PartyManager.INSTANCE.demote(target);
    }

    @WithCondition("minArgs: 1")
    @WithCondition("disableConsole")
    @WithCondition("partyLeaderOnly")
    @WithCondition("withPartyOnly")
    @WithCondition("group: admin")
    @SubcommandExecutor("setting")
    public static void settingCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();
        val party = gamer.getParty();
        val settingName = session.getArgument(0);
        val setting = Party.Setting.valueOf(settingName, null);

        if (setting == null) {
            throw new CommandTranslationException("party_invalid_setting_name");
        }

        // toggling le setting
        party.setSetting(setting, !party.getSetting(setting));
    }

    @WithCondition("disableConsole")
    @WithCondition("withPartyOnly")
    @WithCondition("minArgs: 1")
    @SubcommandExecutor("chat")
    @WithCondition("group: admin")
    @RelayExecutor("pc")
    public static void chatCommand(final MinimindExecutionSession session) throws CommandTranslationException {
        val gamer = (SocialGamer) session.getGamer();
        val party = gamer.getParty();
        if (party.getSetting(Party.Setting.MUTE) &&
                (!party.isLeader(gamer) && !session.getGamer().isStaff())) {
            throw new CommandTranslationException("party_chat_forbidden_mute");
        }

        party.broadcastParty("party_chat_broadcast",
                "%player%", session.getDisplayName(),
                "%message%", session.getArgumentString(0));
    }

}
