package net.skykingdoms.backend.megamind.command;

import lombok.NonNull;
import lombok.val;
import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.exception.CommandMessageException;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.data.sql.sql.MapSqlHandler;
import net.skykingdoms.backend.base.util.color.ChatColor;
import net.skykingdoms.backend.base.util.pair.Pair;
import net.skykingdoms.backend.megamind.AsyncExecution;
import net.skykingdoms.backend.megamind.api.command.CommandSender;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.data.gamer.Gamer;
import net.skykingdoms.backend.megamind.manager.ServerManager;
import net.skykingdoms.backend.megamind.manager.TestFlowManager;
import net.skykingdoms.backend.protocol.type.connect.ConnectStatus;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Command used to deploy test server instances
 * @author Vihara
 */
@NamedCommand("testflow")
public final class TestFlowCommand {

    // @WithCondition("consoleOnly")
    @WithCondition("group: admin")
    @WithCondition("minArgs: 1")
    @Executor
    public static void execute(final @NonNull CoreExecutionSession session) throws CommandMessageException {
        val name = session.getArgument(0);  // name of testflow

        val testFlow = TestFlowManager.INSTANCE.getTestFlow(name);
        if (testFlow == null) {
            throw new CommandMessageException("Testflow configuration is absent! Check name!");
        }

        val options = new Options();

        val input = new Option("m", true, "map");
        input.setRequired(false);
        options.addOption(input);

        val executor = new Option("t", true, "executor");
        executor.setRequired(false);
        options.addOption(executor);

        val instancesOpt = new Option("i", true, "instances");
        instancesOpt.setRequired(false);
        options.addOption(instancesOpt);

        val parser = new DefaultParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, session.getArguments(1));
        } catch (Exception e) {
            throw new CommandMessageException("Invalid arguments!");
        }

        CommandSender sender;
        if (cmd.hasOption("t")) {
            sender = Gamer.getGamer(cmd.getOptionValue("t"));
        } else {
            sender = session.getSender();
        }

        String mapName = null;
        if (cmd.hasOption("m")) {
            val mapData = MapSqlHandler.INSTANCE.loadMap(cmd.getOptionValue("m")).join();
            if (mapData == null) {
                val maps = MapSqlHandler.INSTANCE.getMaps(testFlow.getServerData().getGameMode());
                sender.sendMessage("Unknown map! Try one of these: " + ChatColor.RED + maps.stream()
                        .map(Pair::getFirst)
                        .collect(Collectors.joining(ChatColor.WHITE + ", " + ChatColor.RED))
                );
                return;
            }

            mapName = cmd.getOptionValue("m");
        }

        var instances = 1;
        if (cmd.hasOption("i")) {
            try {
                instances = Integer.parseInt(cmd.getOptionValue("i"));
            } catch (NumberFormatException e) {
                throw new CommandMessageException("Invalid amount of game instances!");
            }
        }

        TestFlowManager.INSTANCE.executeTestFlow(name, sender, mapName, instances, (response, server) -> {
            switch (response) {
                case TESTFLOW_NOT_FOUND:
                    session.sendMessage("Testflow with name {} not found", name);
                    break;
                case TESTFLOW_EXECUTED:
                    sender.sendMessage(ChatColor.GRAY + "Executing test flow " + name + "...");
                    if (sender instanceof Gamer) {
                        val gamer = (Gamer) sender;

 /*                       val party = gamer.getParty();
                        if (party == null) {
                            sender.sendMessage(ChatColor.GRAY + "Btw you dont have a party!");
                        }*/

                        val result = gamer.sendTo(ServerManager.INSTANCE.getPlayableServer(server));

                        if (result == ConnectStatus.SUCCESS) {
                            AsyncExecution.INSTANCE.runLater(() -> {
                                // delay to let party manager warp ppl property

          /*                      if (party != null) {
                                    PartyManager.INSTANCE.warpTo(gamer);
                                }*/

                                sender.sendMessage(ChatColor.GRAY + "Done executing test flow " + name + "!");

                            }, 2500, TimeUnit.MILLISECONDS);
                        }
                    } else {
                        sender.sendMessage(ChatColor.GRAY + "Done executing test flow " + name + "!");
                    }

                    break;
                case TESTFLOW_TRANSACTION_FAILED:
                    session.sendMessage("Testflow transaction failed");
            }
        });
    }

}
