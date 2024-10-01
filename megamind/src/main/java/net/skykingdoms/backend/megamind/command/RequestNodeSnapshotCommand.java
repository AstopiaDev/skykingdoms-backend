package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.WithAlias;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.api.backend.command.tabcomplete.Tabcomplete;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.NodeManager;

/**
 * @author Vihara
 */
@NamedCommand("requestnodesnapshot")
@WithAlias("nodeinfo")
public final class RequestNodeSnapshotCommand {

    @SuppressWarnings("DataFlowIssue")
    @WithCondition("group: admin")
    
    @WithCondition("minArgs: 1")
    @Tabcomplete("[node]")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) throws CommandMessageException {
        val nodeName = session.getArgument(0);
        val nodeInst = NodeManager.INSTANCE.getNodeServer(nodeName);
        if (nodeInst == null) {
            throw new CommandMessageException("Node with this name is absent");
        }

        session.sendMessage("Requesting node snapshot... Please wait... \n");
        val snapshot = NodeManager.INSTANCE.requestSnapshot(nodeName);
        if (snapshot == null) {
            throw new CommandMessageException("Snapshot is null for some reason :/");
        }

        session.sendMessage(
                "Snapshot for {}:\n" +
                "Overall mem: {}\n" +
                "Used mem: {}\n" +
                "Free mem: {}",
                nodeName,
                snapshot.getMaxMemory() / 1024 / 1024,
                snapshot.getUsedMemory() / 1024 / 1024,
                snapshot.getReservedMemory() / 1024 / 1024);
    }

}
