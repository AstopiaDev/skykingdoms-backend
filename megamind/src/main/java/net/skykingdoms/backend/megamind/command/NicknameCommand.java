package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.data.network.NetworkSqlHandler;
import net.skykingdoms.backend.base.data.property.PropertySqlHandler;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@WithAlias("nick")
@NamedCommand("nickname")
public class NicknameCommand {

    private static final List<String> PROHIBITED_WORDS = Arrays.asList(
            "nigger", "nigga", "dick", "muted", "immuted", "mute"
    );

    private static final Pattern VALID_NICKNAME = Pattern.compile("^(&[0-9a-flm-or])*[a-zA-Z_]+(&[0-9a-flm-or]|[a-zA-Z_])*$");

    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 16;

    @Executor
    @WithCondition("minArgs: 1")
    @WithCondition("group: vip")
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        String nickname = session.getArgument(0);

        if (nickname.equalsIgnoreCase("reset")) {
            boolean hasNickname = gamer.getProperty("nickname") != null;
            if (!hasNickname) {
                gamer.sendMessage("§cYou do not have a nickname!");
                return;
            }
            gamer.removeProperty("nickname");
            gamer.sendMessage("§eYour nickname has been reset!");
            return;
        }

        String strippedNickname = nickname.replaceAll("&[0-9a-f-or]", "");
        int target = NetworkSqlHandler.INSTANCE.getPlayerId(strippedNickname);

        if (target != -1) {
            gamer.sendMessage("§cYou cannot be called by someone else's name!");
            return;
        }

        if (strippedNickname.length() < MIN_LENGTH || strippedNickname.length() > MAX_LENGTH) {
            gamer.sendMessage("§cNickname must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters long!");
            return;
        }

        if (!VALID_NICKNAME.matcher(nickname).matches()) {
            gamer.sendMessage("§cInvalid nickname format! Use only letters, underscores, and color codes (e.g., &a, &e, &c).");
            return;
        }

        if (nickname.contains("&k")) {
            gamer.sendMessage("§cYou cannot use the &k color code in your nickname!");
            return;
        }

        String lowercaseStripped = strippedNickname.toLowerCase();
        for (String word : PROHIBITED_WORDS) {
            if (lowercaseStripped.contains(word.toLowerCase())) {
                gamer.sendMessage("§cThis nickname contains inappropriate language!");
                return;
            }
        }

        String coloredNickname = ChatColor.translateAlternateColorCodes('&', nickname);
        gamer.setProperty("nickname", coloredNickname);
        gamer.sendMessage("§ePeople are going to call you " + coloredNickname + " §enow!");
    }
}
