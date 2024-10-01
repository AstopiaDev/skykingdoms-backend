package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.DeploymentManager;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@NamedCommand("deploylobby")
public final class DeployLobbyCommand {

    @WithCondition("group: admin")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(CoreExecutionSession session) throws CommandMessageException {
        val gameType = GameType.valueOf(session.getArgument(0), null);
        if (gameType == null) {
            throw new CommandMessageException("Invalid game type specified! Use one of these: " + Arrays.stream(GameType.VALUES).map(GameType::name).collect(Collectors.joining(", ")));
        }

        DeploymentManager.INSTANCE.sendDeploymentRequest(new BukkitDeployRequest(gameType.bakeLobbyData()),
                ServerDeploy.DeploymentTarget.BUKKIT, false, null, -1,
                (result) -> {
                    if (result == DeploymentResponse.DENIED_BY_NODE
                            || result == DeploymentResponse.DEPLOYMENT_TIMEOUT
                            || result == DeploymentResponse.TRANSACTION_FAILED) {
                        session.sendMessage("Transaction failed! (" + result.name() + ") / (#" + result.transactionId() + ")");
                    } else {
                        session.sendMessage("Queued deployment of lobby for {}!", gameType.getHumanName());
                    }
                });
    }

}
