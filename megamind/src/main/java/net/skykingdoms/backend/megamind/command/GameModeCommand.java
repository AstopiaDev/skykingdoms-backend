package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.command.CommandManager;
import net.skykingdoms.backend.base.command.DynamicCommand;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractPlayerHolder;

public class GameModeCommand extends DynamicCommand {
    GameMode gameMode;
    String name;

    public GameModeCommand(CommandManager manager, String name, GameMode gameMode) {
        super(manager, name, null);
        this.name = name;
        this.gameMode = gameMode;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected <T extends ExecutionSession> void execute(T session) {
        Gamer gamer = (Gamer) session.getSender();

        val target = (AbstractPlayerHolder) ServerManager.INSTANCE.getPersistentServer(gamer, gameMode);

        if (target == null) {
            gamer.sendMessage(ChatColor.RED + "Could not find you a server!");
            return;
        }

        gamer.sendMessage(ChatColor.GREEN + "Sending you to " + gameMode.getHumanName() + "!");
        gamer.sendMessage(gamer.sendTo(target).name());
    }
}
