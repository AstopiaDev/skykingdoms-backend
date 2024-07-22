package net.skykingdoms.backend.base.util.word;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.skykingdoms.backend.base.language.Language;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@EqualsAndHashCode
public class Adjective {

    private final Map<Language, String[][]> forms = new EnumMap<>(Language.class);
    private final Map<Language, String[]> formsPlural = new EnumMap<>(Language.class);

    public Adjective(@NonNull String... forms) {
        if (forms.length != 6 * 4) {
            throw new IllegalArgumentException(Arrays.toString(forms));
        }

        for (Language language : Language.VALUES) {
            String[][] formsArray = this.forms.computeIfAbsent(language, k -> new String[3][6]);
            String[] formsPluralArray = this.formsPlural.computeIfAbsent(language, k -> new String[6]);

            for (int i = 0; i < 6 * 4; i += 4) {
                int row = i / 4;

                formsArray[0][row] = language.getMessage(false, forms[i]);
                formsArray[1][row] = language.getMessage(false, forms[i + 1]);
                formsArray[2][row] = language.getMessage(false, forms[i + 2]);
                formsPluralArray[row] = language.getMessage(false, forms[i + 3]);
            }
        }
    }

    public WordBuilder with(@NonNull Language language, @NonNull Noun noun) {
        return new WordBuilder(language, noun).adj(this);
    }

    public String selectForm(@NonNull Language language, @NonNull WordGender gender, @NonNull WordCase wordCase, boolean isPlural) {
        if (isPlural) {
            return formsPlural.get(language)[wordCase.ordinal()];
        } else {
            return forms.get(language)[gender.ordinal()][wordCase.ordinal()];
        }
    }

    public String toString(@NonNull Language language) {
        return forms.get(language)[0][0];
    }

    @Override
    public String toString() {
        return toString(Language.DEFAULT);
    }

}
