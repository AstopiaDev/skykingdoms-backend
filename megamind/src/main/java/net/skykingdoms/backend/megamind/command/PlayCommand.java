package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.exception.CommandMessageException;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.base.util.optionals.Optional;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.instance.impl.ArenaInstance;
import net.skykingdoms.backend.megamind.manager.InstanceManager;
import net.skykingdoms.backend.megamind.matchmaking.MatchmakingManager;
import net.skykingdoms.backend.megamind.instance.impl.GameInstance;
import net.skykingdoms.backend.protocol.server.GameMode;

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

        Optional<ArenaInstance> optional;
        if (gameMode == GameMode.RANKED_NORMAL) {
            optional = MatchmakingManager.INSTANCE.getOptimalServer(gamer);
        } else {
            optional = Optional.ofNullable((ArenaInstance) InstanceManager.INSTANCE.getBestByOnline(session.getGamer(), gameMode, true));
        }

        if (optional.isEmpty()) {
            throw new CommandMessageException(ChatColor.RED + "Couldn't find a instance for you :(");
        }

        gamer.sendTo(optional.get());
    }

}
