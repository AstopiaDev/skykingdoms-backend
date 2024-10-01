package net.skykingdoms.backend.base.language.loaders;

import lombok.SneakyThrows;
import lombok.val;
import net.skykingdoms.api.protocol.logging.Logging;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.language.parser.I18nParserController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public final class StashLanguageLoader implements LanguageLoader {

    @Override
    @SneakyThrows
    public void loadLanguages() {
        // clear "lang:" parser cache
        I18nParserController.getInstance().clearCache();

        val languageEntries = new HashMap<String, List<String>>();

        val writeBuffer = new ByteArrayOutputStream();

        try (val paths = Files.walk(Paths.get("/stash/localization/localization"))) {
            paths
                    .filter(f -> f.toFile().getName().endsWith(".yml") && !f.toFile().isDirectory())
                    .forEach(f -> {
                        val normalizedPath = f.toAbsolutePath().toString()
                                .replaceFirst("/stash/localization/localization/", "")
                                .split("/");

                        try {
                            writeBuffer.write(Files.readAllBytes(Paths.get(f.toUri())));
                        } catch (IOException e) {
                            Logging.IMP.error("Error reading bytes");
                            throw new RuntimeException(e);
                        }

                        val fileContents = writeBuffer.toString();
                        writeBuffer.reset();

                        languageEntries.computeIfAbsent(normalizedPath[0], __ -> new ArrayList<>())
                                .add(fileContents);

                    });
        }

        Language.RUSSIAN.reload(Collections.emptyMap(), languageEntries.get(Language.RUSSIAN.getShortName()));
        Language.ENGLISH.reload(Language.RUSSIAN.getMessageTranslations(), languageEntries.get(Language.ENGLISH.getShortName()));
        Language.SLOVENIAN.reload(Language.SLOVENIAN.getMessageTranslations(), languageEntries.get(Language.SLOVENIAN.getShortName()));
    }

}
