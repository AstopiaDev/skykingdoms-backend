package net.skykingdoms.backend.base.language;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.val;

import java.util.*;

public class LanguagePaths {

    private final Map<String, Map<String, String[]>> languages;

    public LanguagePaths(JsonObject object) {
        this.languages = new HashMap<>();

        Set<Map.Entry<String, JsonElement>> entries = object.entrySet();

        for (val entry : entries) {
            String languageName = entry.getKey();
            val languageData = entry.getValue().getAsJsonObject();

            Set<Map.Entry<String, JsonElement>> languageDataEntries = languageData.entrySet();
            Map<String, String[]> paths = new HashMap<>(languageDataEntries.size());

            for (Map.Entry<String, JsonElement> languageDataEntry : languageDataEntries) {
                paths.put(languageDataEntry.getKey(), languageDataEntry.getValue().getAsString().toLowerCase()
                        .split(" "));
            }

            languages.put(languageName, paths);
        }
    }

    public Map<String, String[]> getPaths(Language language) {
        return languages.getOrDefault(language.getShortName(), Collections.emptyMap());
    }

    public Collection<String> getPaths(Language language, Collection<String> flags) {
        List<String> foundPaths = new LinkedList<>();
        Map<String, String[]> paths = getPaths(language);

        main:
        for (Map.Entry<String, String[]> entry : paths.entrySet()) {
            // path -> flag1, flag2, flag3
            // if flag1, flag2 and flag3 are contained in flags, this path will be used

            String[] pathFlags = entry.getValue();

            for (String requiredFlag : pathFlags) {
                if (flags.contains(requiredFlag)) {
                    foundPaths.add(entry.getKey());

                    continue main;
                }
            }
        }

        return foundPaths;
    }
}
