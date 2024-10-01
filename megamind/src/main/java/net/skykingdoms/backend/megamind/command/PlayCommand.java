package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.base.util.optionals.Optional;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.matchmaking.MatchmakingManager;
import net.skykingdoms.backend.megamind.server.impl.GameServer;

/**
 * @author Vihara
 */
@NamedCommand("play")
public final class PlayCommand {

    @Tabcomplete("[gameMode]")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) throws CommandMessageException {
        val gameMode = GameMode.valueOf(session.getArgument(0), null);
        val gamer = session.getGamer();
        if (gameMode == null) {
            throw new CommandMessageException(ChatColor.RED + "Invalid game mode!");
        }

        Optional<GameServer> optional;
        if (gameMode == GameMode.RANKED_NORMAL) {
            optional = MatchmakingManager.INSTANCE.getOptimalServer(gamer);
        } else {
            optional = Optional.ofNullable((GameServer) ServerManager.INSTANCE.getBestByOnline(session.getGamer(), gameMode.bakeData(), true));
        }

        if (optional.isEmpty()) {
            throw new CommandMessageException(ChatColor.RED + "Couldn't find a server for you :(");
        }

        gamer.sendTo(optional.get());
    }

}
