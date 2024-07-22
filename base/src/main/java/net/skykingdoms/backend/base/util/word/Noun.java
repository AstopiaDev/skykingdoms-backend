package net.skykingdoms.backend.base.util.word;

import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.language.Language;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EqualsAndHashCode
public class Noun {

    WordGender gender;

    Map<Language, String[]> forms = new EnumMap<>(Language.class);
    Map<Language, String[]> formsPlural = new EnumMap<>(Language.class);

    public Noun(@NonNull WordGender gender, @NonNull String... forms) {
        if (forms.length != 12) {
            throw new IllegalArgumentException(Arrays.toString(forms));
        }

        this.gender = gender;

        for (val language : Language.VALUES) {
            val formsArray = this.forms.computeIfAbsent(language, k -> new String[6]);
            val formsPluralArray = this.formsPlural.computeIfAbsent(language, k -> new String[6]);

            for (var i = 0; i < 6 * 2; i += 2) {
                val row = i / 2;

                formsArray[row] = language.getMessage(false, forms[i]);
                formsPluralArray[row] = language.getMessage(false, forms[i + 1]);
            }
        }
    }

    public WordBuilder get(@NonNull Language language) {
        return new WordBuilder(language, this);
    }

    public WordBuilder get() {
        return new WordBuilder(Language.DEFAULT, this);
    }

    public WordBuilder with(@NonNull Language language, @NonNull Adjective adjective) {
        return get(language).adj(adjective);
    }

    public String selectForm(@NonNull Language language, @NonNull WordCase wordCase, boolean isPlural) {
        String[] currentForms = isPlural ? formsPlural.get(language) : forms.get(language);

        return currentForms[wordCase.ordinal()];
    }

    public String toString(@NonNull Language language) {
        return forms.get(language)[0];
    }

    @Override
    public String toString() {
        return toString(Language.DEFAULT);
    }
}
