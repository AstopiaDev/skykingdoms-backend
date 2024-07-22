package net.skykingdoms.backend.base.util.word;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.base.language.Language;


@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public final class WordBuilder {

    final Language language;
    final Noun noun;
    Adjective adj;

    WordCase wordCase = WordCase.NOMINATIVE;
    boolean isPlural;
    boolean forceNounSingular;

    long number;
    boolean hasNumber;

    boolean firstUpper;
    boolean withoutNoun;
    boolean reverse;

    public WordBuilder adj(Adjective adj) {
        this.adj = adj;
        return this;
    }

    public WordBuilder withCase(WordCase wordCase) {
        this.wordCase = wordCase;
        return this;
    }

    public WordBuilder gen() {
        this.wordCase = WordCase.GENITIVE;
        return this;
    }

    public WordBuilder dat() {
        this.wordCase = WordCase.DATIVE;
        return this;
    }

    public WordBuilder acc() {
        this.wordCase = WordCase.ACCUSATIVE;
        return this;
    }

    public WordBuilder inst() {
        this.wordCase = WordCase.INSTRUMENTAL;
        return this;
    }

    public WordBuilder prep() {
        this.wordCase = WordCase.PREPOSITIONAL;
        return this;
    }

    public WordBuilder single() {
        this.isPlural = false;
        return this;
    }

    public WordBuilder plural() {
        this.isPlural = true;
        return this;
    }

    private enum NumberForm {

        // "1 монета"
        ONE,
        // "2 монеты"
        TWO,
        // "5 монет"
        MANY

    }

    public WordBuilder fromNumber(long number) {
        NumberForm form = NumberForm.MANY;

        number %= 100L;

        if (number < 5L || number > 20L) {
            number %= 10L;

            if (number == 1L) {
                form = NumberForm.ONE;
            }

            if (number == 2L || number == 3L || number == 4L) {
                form = NumberForm.TWO;
            }
        }

        if (this.wordCase == WordCase.NOMINATIVE || this.wordCase == WordCase.ACCUSATIVE) {

            switch (form) {
                case ONE:
                    this.isPlural = false;
                    break;
                case TWO:
                    this.wordCase = WordCase.GENITIVE;
                    this.isPlural = true;
                    this.forceNounSingular = true;
                    break;
                case MANY:
                    this.wordCase = WordCase.GENITIVE;
                    this.isPlural = true;
                    break;
            }
        } else {
            // в остальных падежах падеж остаётся тем же, ставится флаг "мн. ч." если значение > 1
            this.isPlural = form != NumberForm.ONE;
        }

        return this;
    }

    public WordBuilder withNumber(long number) {
        fromNumber(number);

        this.number = number;
        this.hasNumber = true;

        return this;
    }

    public WordBuilder firstUp() {
        this.firstUpper = true;
        return this;
    }

    public WordBuilder withoutNoun() {
        this.withoutNoun = true;
        return this;
    }

    public WordBuilder reverse() {
        this.reverse = true;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        if (this.hasNumber) {
            builder.append(number).append(' ');
        }

        boolean isNounPlural = !this.forceNounSingular && this.isPlural;

        if (reverse) {
            if (!withoutNoun) {
                builder.append(noun.selectForm(language, wordCase, isNounPlural)).append(' ');
            }

            if (adj != null) {
                builder.append(adj.selectForm(language, noun.getGender(), wordCase, isPlural)).append(' ');
            }
        } else {
            if (adj != null) {
                builder.append(adj.selectForm(language, noun.getGender(), wordCase, isPlural)).append(' ');
            }

            if (!withoutNoun) {
                builder.append(this.noun.selectForm(language, wordCase, isNounPlural)).append(' ');
            }
        }

        if (builder.length() > 0) {
            if (firstUpper) {
                builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
            }

            if (builder.charAt(builder.length() - 1) == ' ') {
                builder.setLength(builder.length() - 1);
            }
        }

        return builder.toString();
    }

}
