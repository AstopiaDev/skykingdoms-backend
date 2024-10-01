package net.skykingdoms.backend.megamind.command;

import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.data.state.NetworkState;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ProxyManager;
import net.skykingdoms.backend.megamind.manager.StaffChatManager;
import net.skykingdoms.backend.protocol.packet.play.S28UpdateNetworkState;

/**
 * @author Vihara
 */
@NamedCommand("networkstate")
public final class NetworkStateCommand {

    @WithCondition("consoleOnly") // not to kick all niggas accidentally
    @Executor
    public static void execute(CoreExecutionSession session) throws CommandMessageException {
        if (session.getArgumentCount() == 0) {
            session.sendMessage("Current network state: " + MegamindApp.INSTANCE.networkState().name());
            return;
        }

        val state = NetworkState.getByName(session.getArgument(0));
        if (state == null) {
            throw new CommandMessageException("Invalid network state!");
        }

        if (MegamindApp.INSTANCE.networkState() == state) {
            throw new CommandMessageException("The network is already in " + state.name() + "-state");
        }

        // sending shit to proxies
        ProxyManager.INSTANCE.sendToProxies(new S28UpdateNetworkState(state.ordinal()));

        // updating state on coordinator
        MegamindApp.INSTANCE.networkState(state);

        // calling consumer
        Gamer.getGamers().forEach($ -> state.getOnJoin().accept($));

        StaffChatManager.INSTANCE.sendMessage(ChatColor.WHITE + "Updated network state to " + ChatColor.YELLOW + state.name());
    }

}
