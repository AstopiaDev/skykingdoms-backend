package net.skykingdoms.backend.base.util.word;

import lombok.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CommonWordsLoader {

    private static final Map<String, Object> WORDS = Collections.synchronizedMap(new HashMap<>());

    static {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load common words", e);
        }
    }

    public static void load() throws IOException {
        InputStream in = CommonWordsLoader.class.getResourceAsStream("/words.txt");

        if (in == null) {
            throw new IOException("No words.txt found near CommonWordsLoader class");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;

            String id = null;
            WordGender gender = null;
            List<String> forms = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith(":")) {
                    if (forms != null) {
                        if (gender != null) {
                            WORDS.put(id, new Noun(gender, forms.toArray(new String[0])));
                        } else {
                            WORDS.put(id, new Adjective(forms.toArray(new String[0])));
                        }
                    }

                    line = line.substring(1);

                    int i = line.indexOf(':');

                    if (i != -1) {
                        id = line.substring(0, i);
                        gender = WordGender.valueOf(line.substring(i + 1).toUpperCase());
                    } else {
                        id = line;
                        gender = null;
                    }

                    forms = new ArrayList<>();
                } else if (forms != null) {
                    String[] split = line.split(",");

                    for (String form : split) {
                        form = form.trim();

                        if (!form.isEmpty()) {
                            forms.add(form.toLowerCase());
                        }
                    }
                }
            }

            if (forms != null) {
                if (gender != null) {
                    WORDS.put(id, new Noun(gender, forms.toArray(new String[0])));
                } else {
                    WORDS.put(id, new Adjective(forms.toArray(new String[0])));
                }
            }
        }
    }

    public static Noun getNoun(@NonNull String id) {
        Object o = WORDS.get(id);

        if (!(o instanceof Noun)) {
            throw new IllegalStateException("Word " + id + " not found or not a Noun");
        }

        return (Noun) o;
    }

    public static Adjective getAdj(@NonNull String id) {
        Object o = WORDS.get(id);

        if (!(o instanceof Adjective)) {
            throw new IllegalStateException("Word " + id + " not found or not an Adjective");
        }

        return (Adjective) o;
    }

}
