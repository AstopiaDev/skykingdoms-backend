package net.skykingdoms.backend.base.language.loaders;

import lombok.SneakyThrows;
import lombok.val;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.language.parser.I18nParserController;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public final class GitlabLanguageLoader implements LanguageLoader{

    private static final int PROJECT_ID = 19037479; // changeme :CCC

    private static final String LOCALIZATION_PROJECT_NAME
            = "astrona-localization";

    @Override
    @SneakyThrows
    public void loadLanguages() {

        // clear "lang:" parser cache
        I18nParserController.getInstance().clearCache();

        val url = new URL("https://gitlab.com/api/v4/projects/" + PROJECT_ID + "/repository/archive.zip");

        val connection = url.openConnection();
        connection.setUseCaches(false);
        connection.setRequestProperty("User-Agent", "TravellerLand");
        connection.setRequestProperty("Accept", "*/*");

        val languageEntries = new HashMap<String, List<String>>();

        try (val inputStream = connection.getInputStream();
             val bufferedInputStream = new BufferedInputStream(inputStream, 8192);
             val zipInputStream = new ZipInputStream(bufferedInputStream)) {
            ZipEntry zipEntry;

            val readBuffer = new byte[8192];
            val writeBuffer = new ByteArrayOutputStream();

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                try {
                    val path = zipEntry.getName().split("/");

                    // нам нужно только '**/**/**.yml'
                    if (path.length < 3 || !path[path.length - 1].endsWith(".yml"))
                        continue;

                    int n;

                    while ((n = zipInputStream.read(readBuffer)) != -1) {
                        writeBuffer.write(readBuffer, 0, n);
                    }

                    val fileContents = writeBuffer.toString();
                    writeBuffer.reset();

                    languageEntries.computeIfAbsent(path[1], __ -> new ArrayList<>())
                            .add(fileContents);
                } finally {
                    zipInputStream.closeEntry();
                }
            }
        }

        Language.RUSSIAN.reload(Collections.emptyMap(), languageEntries.get(Language.RUSSIAN.getShortName()));
        Language.ENGLISH.reload(Language.RUSSIAN.getMessageTranslations(), languageEntries.get(Language.ENGLISH.getShortName()));
        Language.SLOVENIAN.reload(Language.SLOVENIAN.getMessageTranslations(), languageEntries.get(Language.SLOVENIAN.getShortName()));
    }
}
