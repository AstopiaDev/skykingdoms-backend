package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.server.AbstractScalable;

/**
 * @author Vihara 
 */
@WithAlias("l")
@WithAlias("д")
@WithAlias("дщиин")
@WithAlias("lobby")
@NamedCommand("hub")
public final class HubCommand {

    @Executor
    public static void execute(CoreExecutionSession session) throws CommandMessageException {
        val currentServer = (AbstractScalable) session.getGamer().getCurrentServer();
        if (currentServer.getGameType() == GameType.MAIN) {
            throw new CommandTranslationException("command_hub_already_in_hub");
        }

        if (session.getArgumentCount() == 1) {
            val targetMode = GameType.valueOf(session.getArgument(0), null);
            if (targetMode == null) {
                throw new CommandMessageException("Invalid target lobby. Use your compass.");
            }

            val server = ServerManager.INSTANCE.getLobbyServer(currentServer.getGameType());
            if (server == null) {
                throw new CommandMessageException("Can't find a lobby server!");
            }

            session.getGamer().sendTo(server);

            return;
        }

        AbstractScalable newServer = null;
        // lobby server for some mini game
        if (currentServer.getServerScale() == ServerScaleType.LOBBY) {
            newServer = ServerManager.INSTANCE.getLobbyServer(GameType.MAIN);
        }

        // player is on game server instance
        if (currentServer.getGameMode() != null) {
            newServer = ServerManager.INSTANCE.getLobbyServer(currentServer.getGameType());
            if (newServer == null) { // no lobby server for gametype, so we route to HUB
                newServer = ServerManager.INSTANCE.getLobbyServer(GameType.MAIN);
            }
        }

        if (newServer == null) {
            throw new CommandMessageException("Can't find a lobby server!");
        }

        session.getGamer().sendTo(newServer);
    }

}
