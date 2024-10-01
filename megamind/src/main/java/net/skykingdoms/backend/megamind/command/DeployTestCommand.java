package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.api.protocol.type.RemoteConnectedType;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.DeploymentManager;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;

@NamedCommand("deploytest")
public final class DeployTestCommand {

    @Tabcomplete("[remoteType]")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) {
        val cock = RemoteConnectedType.valueOf(session.getArgument(0));
        DeploymentManager.INSTANCE.sendDeploymentRequest(cock == RemoteConnectedType.PROXY
                        ? new ProxyDeployRequest() : new BukkitDeployRequest(new ScalableDataImpl(ServerScaleType.LOBBY, GameType.MAIN, null)),
                cock == RemoteConnectedType.PROXY ? ServerDeploy.DeploymentTarget.PROXY : ServerDeploy.DeploymentTarget.BUKKIT, false, null, 0, response -> {
                    session.sendMessage("Result: " + response);
                });
    }

}
