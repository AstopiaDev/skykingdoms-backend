package net.skykingdoms.backend.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;

@NamedCommand("unlink")
public class UnlinkMinecraftCommand {
    @Executor
    public static void execute(CoreExecutionSession session) {
        Gamer gamer = session.getGamer();
        long discordId = gamer.getPropertyOrDefault("discord_id", (long) 0);

        if (discordId == (long) 0 || discordId == (long) -1) {
            gamer.sendMessage("§cYou are not linked to a Minecraft account.");
            return;
        }

        gamer.setProperty("discord_id", (long) 0);
        gamer.sendMessage("§aYou have successfully unlinked your Minecraft account!");
    }
}
