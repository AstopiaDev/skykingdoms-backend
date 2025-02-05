package net.skykingdoms.backend.megamind.manager;


import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.backend.util.collection.CollectionUtil;
import net.skykingdoms.api.backend.util.config.ConfigurationProvider;
import net.skykingdoms.api.backend.util.config.YamlConfiguration;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.api.protocol.server.ServerScaleType;
import net.skykingdoms.api.protocol.server.impl.ScalableDataImpl;
import net.skykingdoms.api.protocol.type.GameMode;
import net.skykingdoms.backend.megamind.MegamindApp;
import net.skykingdoms.backend.megamind.api.command.CommandSender;
import net.skykingdoms.backend.megamind.manager.deployment.DeploymentResponse;
import net.skykingdoms.backend.megamind.manager.testflow.TestFlowData;
import net.skykingdoms.backend.megamind.manager.testflow.TestFlowExecutionResponse;
import net.skykingdoms.backend.megamind.server.AbstractScalable;
import net.skykingdoms.backend.megamind.server.impl.LobbyServer;
import net.skykingdoms.backend.protocol.packet.play.ServerDeploy;
import net.skykingdoms.backend.protocol.server.deploy.impl.BukkitDeployRequest;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class TestFlowManager {

    // ------------------------------------------------------------------
    public static final TestFlowManager INSTANCE = new TestFlowManager();
    // ------------------------------------------------------------------

    /**
     * Used to generate unique testflow ID's
     */
    AtomicInteger ID_COUNTER = new AtomicInteger(0);

    /**
     * Here we store all testflow instances
     */
    Collection<TestFlowData> loadedTests = CollectionUtil.createConcurrentSet();

    /**
     * Here we store transaction ids linked to consumers
     */
    Collection<PendingTestflow> testFlowCallbacks = CollectionUtil.createConcurrentSet();

    /**
     * Invoked once. Loads list of test-flows
     */
    public void postBootstrap() {
        _rescanTestIndexes();
    }

    /**
     * Returns testflow data instance if present.
     */
    public @Nullable TestFlowData getTestFlow(final @NonNull String testName) {
        return loadedTests.stream()
                .filter(testFlowData -> testFlowData.getName().equalsIgnoreCase(testName))
                .findFirst()
                .orElse(null);
    }
    /**
     * Returns testflow data instance if present.
     */
    public @Nullable PendingTestflow getActiveTestFlow(final int id) {
        return testFlowCallbacks.stream()
                .filter(tf -> tf.getTestFlowId() == id)
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    public File createZipFile(final @NonNull TestFlowData data) {
        val tmp = Files.createTempFile(data.getName() + UUID.randomUUID(), ".zip").toFile();
        try {
            val fos = new FileOutputStream(tmp);
            val zos = new ZipOutputStream(fos);

            // Create a Path object for the source directory
            val sourcePath = Paths.get(MegamindApp.INSTANCE.directory()
                    .resolve("../TestFlows/")
                    .resolve(data.getName()).toUri());

            // Traverse the source directory recursively
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            // Get the relative path of the file
                            Path relativePath = sourcePath.relativize(path);

                            // Create a new entry in the zip archive
                            zos.putNextEntry(new ZipEntry(relativePath.toString()));

                            // Read the file and write its contents to the zip entry
                            Files.copy(path, zos);

                            // Close the current entry
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            // Close the zip output stream
            zos.close();
        } catch (Throwable throwable) {
            Logging.IMP.stacktrace(throwable, "[TestFlow] Caught an exception while generating ZIP file for {}", data.getName());
        }

        return tmp;
    }

    /**
     * Basically executes testflow with required params
     */
    public void executeTestFlow(final @NonNull String name,
                                final @NonNull CommandSender executor,
                                final @Nullable String mapName,
                                final int amount,
                                final @Nullable BiConsumer<TestFlowExecutionResponse, String> responseConsumer) {
        _rescanTestIndexes(); // we might change something in TestFlows directory

        val test = getTestFlow(name);
        if (test == null) { // shouldn't happen, because we validate testflow in command
            if (responseConsumer != null) {
                responseConsumer.accept(TestFlowExecutionResponse.TESTFLOW_NOT_FOUND, null);
            }
            return;
        }

        val testFlowId = ID_COUNTER.incrementAndGet();

        Logging.IMP.info("[TestFlow] Preparing test flow {} for {} @ Map: {} @ Instances: {} ID: {} ...",
                name, executor.getName(), mapName == null ? "N/A" : mapName, amount, testFlowId);

        DeploymentManager.INSTANCE.sendDeploymentRequest(new BukkitDeployRequest(test.getServerData()),
                ServerDeploy.DeploymentTarget.BUKKIT, true, name, testFlowId,
                (result) -> {
                    if (result == DeploymentResponse.DENIED_BY_NODE
                            || result == DeploymentResponse.DEPLOYMENT_TIMEOUT
                            || result == DeploymentResponse.TRANSACTION_FAILED) {
                        Logging.IMP.info("[TestFlow/D] Response null, testflow failed");
                        if (responseConsumer != null) {
                            Logging.IMP.info("[TestFlow/D] Testflow failed");
                            responseConsumer.accept(TestFlowExecutionResponse.TESTFLOW_TRANSACTION_FAILED, null);
                        }
                    } else {
                        Logging.IMP.info("[TestFlow/D] Response null, testflow success");
                        if (responseConsumer != null) {
                            Logging.IMP.info("[TestFlow/D] Adding pending testflow");
                            testFlowCallbacks.add(new PendingTestflow(executor, result.transactionId(), testFlowId, test, responseConsumer, mapName, amount));
                        }
                    }
                });
    }

    public String getTestflowMap(final @NonNull AbstractScalable shit) {
        return testFlowCallbacks.stream().filter(cock -> cock.getTestFlowId() == shit.getLinkedTestFlowId())
                .map(PendingTestflow::getMapName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public void handleServerAction(final @NonNull ServerAction action,
                                   final int testFlowId,
                                   final @NonNull AbstractScalable pendingServer) {
        val testflow = testFlowCallbacks.stream()
                .filter(tf -> tf.getTestFlowId() == testFlowId)
                .findFirst()
                .orElse(null);

        if (testflow == null) {
            Logging.IMP.info("[TestFlow/C] Null testflow ID:" + testFlowId);
            return;
        }

        Logging.IMP.info("[TestFlow/C] Got some shit checks");

        val data = testflow.getData().getServerData();

        // we await lobby, but got some shit
        if (data.getServerScale() == ServerScaleType.LOBBY && !(pendingServer instanceof LobbyServer)) {
            Logging.IMP.info("Not instance of lobby");
            return;
        }

        // we await game instance, but got some shit
        if ((data.getServerScale() == ServerScaleType.MINI || data.getServerScale() == ServerScaleType.MEGA)) {
            return;
        }

        // basically removing shit from pre-loaded cock
        if (action == ServerAction.SHUTDOWN) {
            testFlowCallbacks.remove(testflow);
        }

        testflow.getConsumer().accept(TestFlowExecutionResponse.TESTFLOW_EXECUTED, pendingServer.getName());
    }

    /**
     * Basically we process "./TestFlow" directory and
     * add all the stuff we got to our data collection
     */
    @SneakyThrows
    private void _rescanTestIndexes() {
        loadedTests.clear(); // cleaning up old shit

        val testFlowsDir = MegamindApp.INSTANCE.directory().resolve("../").resolve("TestFlows");
        val testFlowNames = new HashSet<String>();

        if (!testFlowsDir.toFile().exists()) {
            return;
        }

        // scanning the directory to get all testflow names
        Files.walkFileTree(testFlowsDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                // dolbaeb fix :<
                // todo: refactor
                if (dir.getFileName().toString().equals("TestFlows")) return FileVisitResult.CONTINUE;
                val len = dir.toFile().getAbsolutePath().split("/").length;
                // Logging.IMP.info(len + " -> " + dir.toFile().getAbsolutePath().toString());
                if (len > 8) {
                    return FileVisitResult.CONTINUE;
                }

                testFlowNames.add(dir.getFileName().toString());
                return FileVisitResult.CONTINUE;
            }
        });

        val provider = ConfigurationProvider.getProvider(YamlConfiguration.class);
        for (val testFlowName : testFlowNames) {
            val path = testFlowsDir.resolve(testFlowName);
            val configFile = provider.load(path.resolve("testflow.yml").toFile());

            Logging.IMP.info("[TestFlow/D] N: {} @ C: {}", testFlowName, configFile.toString());

            try {
                loadedTests.add(new TestFlowData(
                        testFlowName,
                        new ScalableDataImpl(
                                ServerScaleType.valueOf(configFile.getString("server-scale")),
                                GameType.valueOf(configFile.getString("game-type", null)),
                                GameMode.valueOf(configFile.getString("game-mode"), null)
                        ),
                        configFile.getBoolean("debug"),
                        configFile.getInt("debug-port", 0)
                ));
            } catch (Exception e) {
                Logging.IMP.stacktrace(e, "[TestFlow] An exception raised on testflow config processing");
            }
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor
    @Getter @Setter
    public static final class PendingTestflow {

        CommandSender executor;

        UUID transactionId;

        int testFlowId;

        TestFlowData data;

        BiConsumer<TestFlowExecutionResponse, String> consumer;

        String mapName;

        int amount;

    }

    public enum ServerAction {
        ADD_BUKKIT, ADD_LOBBY, ADD_GAME, SHUTDOWN
    }

}