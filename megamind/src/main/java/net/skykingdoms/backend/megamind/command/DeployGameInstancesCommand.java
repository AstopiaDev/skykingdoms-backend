package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.DeploymentManager;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.type.GameBootResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Vihara
 */
@NamedCommand("deploygameinstances")
public final class DeployGameInstancesCommand {

    @WithCondition("group: admin")
    
    @WithCondition("minArgs: 2")
    @Executor
    public static void execute(CoreExecutionSession session) throws CommandMessageException {
        val gameType = GameType.valueOf(session.getArgument(0), null);
        if (gameType == null) {
            throw new CommandMessageException("Invalid game type specified! Use one of these: " + Arrays.stream(GameType.VALUES).map(GameType::name).collect(Collectors.joining(", ")));
        }

        val responses = new ArrayList<GameBootResponse>();
        val _comps = session.getArguments(1);
        for (val component : _comps) {
            val comps = component.split("=");
            responses.add(new GameBootResponse(GameMode.valueOf(comps[0]), Integer.parseInt(comps[1])));
        }

        if (responses.size() == 0) {
            throw new CommandMessageException("Empty game modes!");
        }

        session.sendMessage("Bootstrapping cluster of minigame instances of: ");
        responses.forEach(response -> {
            session.sendMessage(response.getGameMode().getHumanName() + " -> " + response.getAmount());
        });

        DeploymentManager.INSTANCE.sendDeploymentRequest(new BukkitDeployRequest(responses.get(0).getGameMode().bakeData()),
                ServerDeploy.DeploymentTarget.BUKKIT, false, null, -1,
                (result) -> {
                    if (result == DeploymentResponse.DENIED_BY_NODE
                            || result == DeploymentResponse.DEPLOYMENT_TIMEOUT
                            || result == DeploymentResponse.TRANSACTION_FAILED) {
                        session.sendMessage("Transaction failed! (" + result.name() + ") / (#" + result.transactionId() + ")");
                    } else {
                        ServerManager.INSTANCE.queueGameInstanceBoot(result.transactionId(), responses.toArray(GameBootResponse[]::new));
                        session.sendMessage("Queued deployment of game instances!");
                    }
                });
    }

}
