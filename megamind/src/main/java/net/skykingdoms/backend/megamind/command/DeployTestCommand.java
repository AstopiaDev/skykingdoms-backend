package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.backend.base.command.NamedCommand;
import net.skykingdoms.backend.base.command.condition.WithCondition;
import net.skykingdoms.backend.base.command.execution.Executor;
import net.skykingdoms.backend.base.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.DeploymentManager;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import net.skykingdoms.backend.protocol.server.deploy.impl.ProxyDeployRequest;
import net.skykingdoms.backend.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.backend.protocol.type.RemoteConnectedType;

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
