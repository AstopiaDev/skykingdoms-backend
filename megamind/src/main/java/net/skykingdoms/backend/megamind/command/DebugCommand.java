package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.RelayExecutor;
import net.skykingdoms.backend.base.command.RelayExecutorAlias;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandException;
import net.skykingdoms.backend.base.command.exception.CommandMessageException;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.execution.SubcommandExecutor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.data.friends.FriendshipMeta;
import net.skykingdoms.backend.base.data.settings.SettingType;
import net.skykingdoms.backend.base.data.settings.impl.BooleanSetting;
import net.skykingdoms.backend.base.data.settings.impl.EnumSetting;
import net.skykingdoms.backend.base.permission.Group;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.friend.FriendManager;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.api.protocol.type.PropertyType;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This command is used for debug purposes.
 *
 * @author Vihara
 */
@WithAlias("бубен")
@WithAlias("buben")
@NamedCommand("debug")
public final class DebugCommand {

    @WithCondition("group: admin")
    
    @Executor
    public static void execute(final CoreExecutionSession session) {

        session.sendMessage("Invoked debug command. Args: " + Arrays.toString(session.getArguments()));
    }

    @SubcommandExecutor("dumpgamers")
    @WithCondition("group: admin")
    
    public static void dumpGamers(final CoreExecutionSession session) {
        session.sendMessage(Gamer.getGamers().stream().map(g -> g.getName() + "/" + g.getGamerId()).collect(Collectors.joining(", ")));
    }

    @SubcommandExecutor("unload")
    @WithCondition("group: admin")
    
    public static void unload(final CoreExecutionSession session) throws CommandTranslationException {
        val gamer = session.getGamer(0);
        gamer.quit();
        session.sendMessage("Unloaded " + gamer.getName());
    }

    @WithCondition("disableConsole")
    @WithCondition("group: admin")
    
    @SubcommandExecutor("ranks")
    public static void testRanks(final CoreExecutionSession session) {
        session.sendMessage(session.getGamer().getRanks().stream().map(ranks -> {
            return "RID: " + ranks.getId() + " - RL: " +
                    ranks.getLevel() + " - RGN: " + ranks.getGroup().getName() + ChatColor.WHITE +
                    " - RGL: " + ranks.getGroup().getLevel() + " - RAC: \n"
                    + Arrays.stream(Group.VALUES).map(g -> g.getName() + "=" + ranks.canApply(g)).collect(Collectors.joining("\n"));
        }).collect(Collectors.joining("\n------")));
    }

    @WithCondition("group: admin")
    @WithCondition("minArgs: 2")
    @Tabcomplete("[playerName] [property]")
    
    @SubcommandExecutor("prop")
    public static void getProperty(final CoreExecutionSession session) throws CommandMessageException {
        val target = session.getOfflineGamer(0, Gamer::loadProperties);
        val prop = PropertyType.valueOf(session.getArgument(1), null);
        if (prop == null) {
            throw new CommandMessageException("dolbaeb");
        }

        session.sendMessage(prop.name() + " -> " + target.getProperty(prop));
    }

    @WithCondition("group: admin")
    @WithCondition("minArgs: 2")
    @Tabcomplete("[playerName] [setting]")
    
    @SubcommandExecutor("setting-get")
    public static void getSetting(final CoreExecutionSession session) throws CommandMessageException {
        val target = session.getOfflineGamer(0, Gamer::loadProperties);
        val setting = SettingType.valueOf(session.getArgument(1), null);
        if (setting == null) {
            throw new CommandMessageException("dolbaeb");
        }

        session.sendMessage(setting.name() + " -> " + target.getSetting(setting).getValue());
    }

    @WithCondition("group: admin")
    
    @SubcommandExecutor("friends")
    public static void debugFriends(final CoreExecutionSession session) throws CommandException {
        if (session.getArgumentCount() > 0) {
            val option = session.getArgument(0);
            val one = session.getIntArgument(1, "first");
            val two = session.getIntArgument(2, "second");
            if (option.equals("create")) {
                session.sendMessage("C -> " + FriendManager.INSTANCE.sendFriendRequest(one, two));
                return;
            }

            if (option.equals("accept")) {
                session.sendMessage("A -> " + FriendManager.INSTANCE.acceptFriendRequest(one, two));
                return;
            }

            if (option.equals("deny")) {
                session.sendMessage("D -> " + FriendManager.INSTANCE.denyRequest(one, two));
                return;
            }

            if (option.equals("remove")) {
                session.sendMessage("R -> " + FriendManager.INSTANCE.removeFriend(one, two));
                return;
            }
        }
        session.sendMessage("-------------------------------");
        session.sendMessage("FriendsManager dump:");
        session.sendMessage("\n" + FriendManager.INSTANCE.getFriendshipMetas().stream().map(FriendshipMeta::toString).collect(Collectors.joining("\n")));
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    @WithCondition("group: admin")
    @WithCondition("minArgs: 3")
    @Tabcomplete("[playerName] [setting]")
    
    @SubcommandExecutor("setting-set")
    public static void setSetting(final CoreExecutionSession session) throws CommandMessageException {
        val target = session.getOfflineGamer(0, Gamer::loadProperties);
        val setting = SettingType.valueOf(session.getArgument(1), null);
        if (setting == null) {
            throw new CommandMessageException("dolbaeb");
        }

        if (setting.getSetting() instanceof BooleanSetting) {
            val settingInst = (BooleanSetting) session.getGamer().getSetting(setting);
            settingInst.setValue(Boolean.parseBoolean(session.getArgument(2)));

            target.setSetting(setting, settingInst);
        }

        if (setting.getSetting() instanceof EnumSetting<?>) {
            val enumSetting = (EnumSetting) session.getGamer().getSetting(setting);
            enumSetting.setValue(Enum.valueOf(enumSetting.getEnumClass(), session.getArgument(2)));

            target.setSetting(setting, enumSetting);
        }

        session.sendMessage("Updated: " + setting.name() + " -> " + target.getSetting(setting).getValue());
    }

    @RelayExecutorAlias({ "tf", "cock" })   // experimental
    @RelayExecutor("testframework")         // experimental
    @WithCondition("group: admin")
    @WithCondition("minArgs: 1")
    
    @Tabcomplete("[setting]")
    public static void executeFrameworkTest(final CoreExecutionSession session) {
        session.sendMessage("A -> " + Arrays.toString(session.getArguments()));
        session.sendMessage("W -> " + session.getExecutor().toString());
    }

/*    @WithCondition("group: admin")
    
    @SubcommandExecutor("party")
    public static void executePartyDebug(final CoreExecutionSession session) {
        session.sendMessage("----------------------------");
        if (!session.isConsole()) {
            session.sendMessage("Your party: " + session.getGamer().getParty());
        }
        session.sendMessage("PartyManager dump: ");
        for (val party : PartyManager.INSTANCE.getParties().int2ObjectEntrySet()) {
            session.sendMessage("#" + party.getIntKey() + " - " + party.getValue());
        }
    }*/
}
