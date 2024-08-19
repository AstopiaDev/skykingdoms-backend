package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.WithAlias;
import net.skykingdoms.backend.base.command.exception.CommandMessageException;
import net.skykingdoms.backend.base.command.exception.CommandTranslationException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.instance.impl.LobbyInstance;
import net.skykingdoms.backend.megamind.manager.LobbyManager;
import net.skykingdoms.backend.megamind.instance.AbstractScalable;
import net.skykingdoms.backend.protocol.server.GameType;

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

            val server = LobbyManager.INSTANCE.getLobbyInstance(currentServer.getGameType());
            if (server == null) {
                throw new CommandMessageException("Can't find a lobby instance!");
            }

            session.getGamer().sendTo(server);

            return;
        }

        AbstractScalable newServer = null;
        // lobby instance for some mini game
        if (currentServer instanceof LobbyInstance) {
            newServer = LobbyManager.INSTANCE.getLobbyInstance(GameType.MAIN);
        }

        // player is on game instance instance
        if (currentServer.getGameMode() != null) {
            newServer = LobbyManager.INSTANCE.getLobbyInstance(currentServer.getGameType());
            if (newServer == null) { // no lobby instance for gametype, so we route to HUB
                newServer = LobbyManager.INSTANCE.getLobbyInstance(GameType.MAIN);
            }
        }

        if (newServer == null) {
            throw new CommandMessageException("Can't find a lobby instance!");
        }

        session.getGamer().sendTo(newServer);
    }

}
