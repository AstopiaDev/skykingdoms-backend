package net.skykingdoms.backend.megamind.command;

import net.skykingdoms.api.backend.command.NamedCommand;
import net.skykingdoms.api.backend.command.condition.WithCondition;
import net.skykingdoms.api.backend.command.execution.Executor;
import net.skykingdoms.backend.base.data.sql.GameMap;
import net.skykingdoms.backend.base.data.sql.sql.MapSqlHandler;
import net.skykingdoms.backend.megamind.api.command.CoreExecutionSession;
import net.skykingdoms.backend.megamind.manager.StashManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@NamedCommand("insertmap")
public class InsertMapCommand {

    @Executor
    @WithCondition("group: owner")
    @WithCondition("minArgs: 3")
    public static void execute(CoreExecutionSession session) {
        String mapName = session.getArgument(0);
        String humanName = session.getArgument(1);
        String modes = session.getArgument(2);

        File zippedFile = StashManager.INSTANCE.createZipFileFromOneSource(mapName);

        try {
            byte[] fileBytes = Files.readAllBytes(zippedFile.toPath());
            MapSqlHandler.INSTANCE.saveMap(new GameMap(
                    mapName,
                    humanName,
                    fileBytes
            ), modes);
            session.sendMessage("Map {} has been successfully inserted into the database.", mapName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
