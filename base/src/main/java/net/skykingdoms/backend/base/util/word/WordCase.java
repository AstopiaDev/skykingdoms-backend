package net.skykingdoms.backend.base.util.word;

import lombok.Getter;

@Getter
public enum WordCase {

    /**
     * Именительный
     */
    NOMINATIVE("есть"),
    /**
     * Родительный
     */
    GENITIVE("нет"),
    /**
     * Дательный
     */
    DATIVE("дать"),
    /**
     * Винительный
     */
    ACCUSATIVE("винить"),
    /**
     * Творительный
     */
    INSTRUMENTAL("творить"),
    /**
     * Предложный
     */
    PREPOSITIONAL("о");

    /**
     * For debug purposes
     */
    private final String question;

    WordCase(String question) {
        this.question = question;
    }
}
