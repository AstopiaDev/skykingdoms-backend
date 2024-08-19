package net.skykingdoms.backend.bukkit.startup.core;

import de.leonhard.storage.Yaml;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import net.skykingdoms.backend.base.util.config.Configuration;
import net.skykingdoms.backend.protocol.logging.Logging;
import net.skykingdoms.backend.protocol.server.GameMode;
import net.skykingdoms.backend.protocol.server.GameType;
import net.skykingdoms.backend.protocol.server.ServerScaleType;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The bootstrap magic happens here wooooo
 */
public final class BukkitAgent {
    // ALL servers have this param
    ServerScaleType serverScaleType;

    // Actually, I guess all the servers have this present
    // exceptions: limbo instance or some unhandled exceptions
    GameType gameType;

    // Might be null, if that's not a game instance
    GameMode gameMode;
    JavaVersion javaVersion;

    // Debug port for testflow
    // -1 if not debugging
    int debugPort;

    // linked testflow id
    // <= 0 if not a testflow serer
    int testFlowId;

    // NULL if not testflow deployment
    TestFlowData testFlowData;
    Configuration configuration;

    public boolean isGameServer() {
        return (serverScaleType == ServerScaleType.LARGE || serverScaleType == ServerScaleType.MEDIUM)
                && gameMode != null;
    }

    public boolean isLobby() {
        return serverScaleType == ServerScaleType.MINI;
    }

    public int getPort() {
        return Integer.parseInt(configuration == null ? System.getenv("BUKKIT_HOST").split(":")[1] : configuration.getString("BUKKIT_HOST").split(":")[1]);
    }

    public String getLocalHost() {
        return configuration == null ? System.getenv("BUKKIT_HOST").split(":")[0] : configuration.getString("BUKKIT_HOST").split(":")[0];
    }

    public boolean isTestFlow() {
        return System.getenv().containsKey("TEST_FLOW");
    }

    /**
     * TestFlow name
     */
    public @Nullable String getTestFlowName() {
        return isTestFlow() ? System.getenv("TEST_FLOW") : null;
    }

    private static String externalHost;

    @SneakyThrows
    public String getExternalHost() {
        if (externalHost == null) {
            val url = new URL("https://checkip.amazonaws.com");
            val in = new BufferedReader(new InputStreamReader(
                    url.openStream()));

            externalHost = in.readLine();
        }

        return externalHost;
    }

    /**
     * Invoked once from bootstrapper class
     */
    @SneakyThrows
    public void bootstrap(
            final @NonNull ServerScaleType serverScaleType,
            final @NonNull GameType gameType,
            final @Nullable GameMode gameMode,
            final Configuration configuration,
            JavaVersion javaVersion) {
        this.serverScaleType = serverScaleType;
        this.gameType = gameType;
        this.gameMode = gameMode;
        if (configuration != null) {
            this.configuration = configuration;
        }
        if (javaVersion != null) {
            this.javaVersion = javaVersion;
        }

        Logging.IMP.info("[boot] Dumped ENV: {}", System.getenv().entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining(", ")));
        Logging.IMP.info("[boot] Agent args: {} @ {} @ {}", serverScaleType, gameType, gameMode);
        Logging.IMP.info("[boot] GS: {} @ L: {}", isGameServer(), isLobby());
        Logging.IMP.info("[boot] Local: {} @ External: {}", getLocalHost() + ":" + getPort(), getExternalHost());
        Logging.IMP.info("[boot] TestFlow: {}{}", isTestFlow(), isTestFlow() ? "-> " + getTestFlowName() : "");

        Logging.IMP.info("[boot] Downloading global assets [...]");

        // Downloading all shit assets
        processDefaultDeployment(gameType);

        Logging.IMP.info("[boot] Shit downloaded uf-af :3 [ok]");

        if (isLobby()) {
            Logging.IMP.info("[boot] Detected lobby configuration, downloading shit [...]");
            downloadLobbyAssets(gameType);
            Logging.IMP.info("[boot] Probably downloaded shit idk xd [ok (navernoe)]");
        }

        if (isTestFlow()) {
            Logging.IMP.info("[boot] Processing testflow data [...]");
            processTestflow(Objects.requireNonNull(getTestFlowName()));
            if (debugPort > 0) {
                Logging.IMP.info("[boot] debugging on {}:{}", getExternalHost(), debugPort);
            }
            Logging.IMP.info("[boot] TestFlow ID: {}", testFlowId);
            Logging.IMP.info("[boot] Done processing testflow data [ok]");
        }

        Logging.IMP.info("[boot] Modifying instance.properties [...]");
        modifyServerProperties();
        Logging.IMP.info("[boot] Modified instance.properties [ok]");

        Logging.IMP.info("[boot] Executing core bootstrap [...]");
        executeCore();
        Logging.IMP.info("[boot] End of bukkit agent driver process! Byeeee! :3");

        exec("read -p \"Press any key to resume ...\"");
    }

    @SneakyThrows
    public void modifyServerProperties() {
        val props = new Properties();
        props.load(new FileReader("instance.properties"));
        props.setProperty("instance-port", String.valueOf(getPort()));
        props.setProperty("online-mode", String.valueOf(false));
        props.setProperty("enforce-secure-profile", String.valueOf(false));
/*        if (serverScaleType != ServerScaleType.PERSISTENT) {
            props.setProperty("allow-nether", String.valueOf(false));
        }*/
        props.setProperty("motd", "static-connect\n" + serverScaleType.name() + "/" + gameType.name());
        props.store(new FileWriter("instance.properties"), null);
    }

    public void executeCore() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        // Get the MemoryMXBean for memory management
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        // Get the heap memory usage
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();

        // Initial heap size (Xms)
        long initialHeapSize = heapMemoryUsage.getInit();

        // Maximum heap size (Xmx)
        long maxHeapSize = heapMemoryUsage.getMax();

        // Convert bytes to megabytes
        long initialHeapSizeMB = initialHeapSize / (1024 * 1024);
        long maxHeapSizeMB = maxHeapSize / (1024 * 1024);

        List<String> command = new ArrayList<>();

        command.add(javaVersion== null ? "java" : javaVersion.getBinaryPath());
        command.add("-Xms" + initialHeapSizeMB + "m");
        command.add("-Xmx" + maxHeapSizeMB + "m");
        command.add(debugPort > 0 ? "-agentlib:jdwp=transport=dt_socket,instance=y,suspend=n,address=*:" + debugPort : "-Dvoid=0");

        if (javaVersion != null && javaVersion == JavaVersion.GRAALVM_17 || javaVersion == JavaVersion.NORMAL_17 || javaVersion == JavaVersion.OPENJDK_21) {
            command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED");
            command.add("--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED");
            command.add("--add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED");
        }

        command.add("-Dfile.encoding=UTF-8");
        command.add("-DserverScale=" + serverScaleType.name());
        command.add("-DgameType=" + gameType.name());
        command.add("-DtestFlowId=" + testFlowId);
        command.add("-DgameMode=" + (gameMode == null ? "NULL" : gameMode.name()));
        command.add("-Dlog4j2.formatMsgNoLookups=true");
        command.add("-DexternalAddr=" + getExternalHost());
        command.add("-jar");
        command.add("instance.jar");

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try {
            Process processRun = processBuilder.start();
            System.out.println(processRun.info().command().orElse("no command?") + " " + Arrays.toString(processRun.info().arguments().get()));

            // Handle the output and error streams
            new Thread(() -> printLines(processRun.getInputStream())).start();
            new Thread(() -> printLines(processRun.getErrorStream())).start();

            // Handle the input stream
            BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
            OutputStream processInput = processRun.getOutputStream();
            new Thread(() -> {
                try {
                    String userInput;
                    while ((userInput = userInputReader.readLine()) != null) {
                        processInput.write((userInput + System.lineSeparator()).getBytes());
                        processInput.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Wait for the process to terminate
            int exitCode = processRun.waitFor();
            System.out.println("Process terminated with exit code: " + exitCode);

            // Exit the main application with the same exit code
            System.exit(exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Exit the main application with a non-zero exit code to indicate an error
            System.exit(1);
        }
    }

    private void printLines(InputStream inputStream) {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(System.out::println);
    }

    private void exec(final @NonNull String shell) {
        exec(shell.split(" "));
    }

    private Process exec(final String @NonNull [] shell) {
        try {
            return Runtime.getRuntime().exec(shell);
        } catch (final Exception e) {
            Logging.IMP.stacktrace(e, "Runtime execution exception caught!");
            return null;
        }
    }

    @SneakyThrows
    private void processDefaultDeployment(final @NonNull GameType gameType) {
        val epoch = System.currentTimeMillis();

        // not using cathttputil here, #StayLegacy
        val instanceShit = exec(new String[]{
                "curl",
                "-H",
                "Authorization: Basic Zkk3ODAhfiNpZ0g2TyFJdlZgVDMkI34=",
                "-v",
                "http://95.216.43.103:8000/api/retrieve?token=cock&name=" + gameType.name(),
                "--output",
                "stash.zip"});

        printLines(instanceShit.getInputStream());
        printLines(instanceShit.getErrorStream());

        printLines(exec(new String[]{ "unzip", "-o", "stash.zip" }).getErrorStream());

        Logging.IMP.info("[boot] Fetched stashed assets in " + (System.currentTimeMillis() - epoch));
    }

    @SneakyThrows
    private void downloadLobbyAssets(final @NonNull GameType gameType) {
        val epoch = System.currentTimeMillis();

        val lobbyAssets = exec(new String[]{
                "curl",
                "-H",
                "Authorization: Basic Zkk3ODAhfiNpZ0g2TyFJdlZgVDMkI34=",
                "-v",
                "http://95.216.43.103:8000/api/retrieve?token=cock&name=" + gameType.name() + "&lobby=dadadaaaa", // todo: implement config for api ip blabla cba rn doesnt matter that much u can know the ip and shit
                "--output",
                "lobby.zip"});

        printLines(lobbyAssets.getInputStream());
        printLines(lobbyAssets.getErrorStream());

        printLines(exec(new String[]{ "unzip", "-o", "lobby.zip" }).getErrorStream());

        Logging.IMP.info("[boot] Fetched lobby assets in " + (System.currentTimeMillis() - epoch));
    }

    @SneakyThrows
    private void processTestflow(final @NonNull String name) {
        val epoch = System.currentTimeMillis();

        // also not using cathttputil here, #StayLegacy !!!!
        val shit = exec(new String[]{
                "curl",
                "-H",
                "Authorization: Basic Zkk3ODAhfiNpZ0g2TyFJdlZgVDMkI34=",
                "-v",
                "http://95.216.43.103:8000/api/testflow?token=cock&name=" + name,
                "--output",
                "testflow.zip"});

        printLines(shit.getInputStream());
        printLines(shit.getErrorStream());

        printLines(exec(new String[]{ "unzip", "-o", "testflow.zip" }).getErrorStream());
        printLines(exec(new String[]{ "mkdir", "plugins" }).getErrorStream());

        testFlowData = readTestFlowData(new File("testflow.yml"));
        for (val relocation : testFlowData.getRelocations().entrySet()) {
            printLines(exec(new String[]{"cp", "-rf", relocation.getKey(), relocation.getValue()}).getErrorStream());
        }

        Logging.IMP.info("[boot] Fetched testflow assets in " + (System.currentTimeMillis() - epoch));
    }

    @SneakyThrows
    private TestFlowData readTestFlowData(final @NonNull File file) {
        val testFlowData = new TestFlowData();

        // this shit allows us to load testflow.yml
        val yaml = new Yaml(file.getName(), "");

        if (yaml.contains("relocation")) {
            for (val entry : yaml.getMap("relocation").entrySet()) {
                val key = (String) entry.getKey();
                val target = (String) entry.getValue();
                testFlowData.addRelocation(key, target);
            }
        }

        this.debugPort = yaml.getInt("debug-port");

        // not configurable value
        this.testFlowId = Integer.parseInt(System.getenv("TEST_FLOW_ID"));

        testFlowData.setDebugPort(yaml.getInt("debug-port"));
        testFlowData.setDebug(yaml.getBoolean("debug"));

        return testFlowData;
    }

}
