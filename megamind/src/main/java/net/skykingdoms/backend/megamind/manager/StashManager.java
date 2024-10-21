package net.skykingdoms.backend.megamind.manager;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.api.protocol.server.GameType;
import net.skykingdoms.backend.megamind.MegamindApp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 */
public final class StashManager {

    public static final StashManager INSTANCE = new StashManager();

    @SneakyThrows
    public File createZipFile(final @NonNull String data) {
        val tmp = Files.createTempFile(MegamindApp.INSTANCE.directory().resolve("temp"), data + UUID.randomUUID(), ".zip").toFile();
        try (val fos = new FileOutputStream(tmp);
             val zos = new ZipOutputStream(fos)) {

            Logging.IMP.info(data);

            Path sourcePath = Paths.get(MegamindApp.INSTANCE.directory()
                    .resolve("stash")
                    .resolve(data).toUri());

            Logging.IMP.info(sourcePath.toFile().exists() + " S?");

            Path globalsPath = Paths.get(MegamindApp.INSTANCE.directory()
                    .resolve("stash/_GLOBAL")
                    .toUri());

            Logging.IMP.info(globalsPath.toFile().exists() + " G?");

            if (Files.walk(sourcePath).filter(path -> !Files.isDirectory(path)).count() != 0) {
                Files.walk(sourcePath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            try {
                                val relativePath = sourcePath.relativize(path);
                                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }

            Files.walk(globalsPath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            val relativePath = globalsPath.relativize(path);
                            zos.putNextEntry(new ZipEntry(relativePath.toString()));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        } catch (Throwable throwable) {
            Logging.IMP.stacktrace(throwable, "Shit happens :(");
        }

        return tmp;
    }

    @SneakyThrows
    public File createZipFileFromOneSource(final @NonNull String data) {
        val tmp = Files.createTempFile(MegamindApp.INSTANCE.directory().resolve("temp"), data + UUID.randomUUID(), ".zip").toFile();
        try (val fos = new FileOutputStream(tmp);
             val zos = new ZipOutputStream(fos)) {

            Path sourcePath = Paths.get(MegamindApp.INSTANCE.directory()
                    .resolve("stash")
                    .resolve(data).toUri());

            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            val relativePath = sourcePath.relativize(path);
                            zos.putNextEntry(new ZipEntry(relativePath.toString()));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        } catch (Throwable throwable) {
            Logging.IMP.stacktrace(throwable, "Shit happens :(");
        }

        return tmp;
    }

    @SneakyThrows
    public File getLobbyAssets(final @NonNull GameType gameType) {
        val tmp = Files.createTempFile(MegamindApp.INSTANCE.directory().resolve("temp"), gameType.name() + "-lobby-" + UUID.randomUUID(), ".zip").toFile();
        try (val fos = new FileOutputStream(tmp);
             val zos = new ZipOutputStream(fos)) {

            Path lobbies = Paths.get(MegamindApp.INSTANCE.directory()
                    .resolve("stash/_LOBBIES")
                    .resolve(gameType.name())
                    .toUri());

            Files.walk(lobbies)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            val relativePath = lobbies.relativize(path);
                            zos.putNextEntry(new ZipEntry(relativePath.toString()));
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (Throwable throwable) {
            Logging.IMP.stacktrace(throwable, "Shit happens :(");
        }

        return tmp;
    }
}