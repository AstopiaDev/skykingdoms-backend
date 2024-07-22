package net.skykingdoms.backend.base.util.word;

public enum WordGender {

    MASCULINE,
    FEMININE,
    NEUTER;

    public String select(String m, String f, String n) {
        switch (this) {
            case MASCULINE:
                return m;
            case FEMININE:
                return f;
            case NEUTER:
                return n;
            default:
                throw new IllegalStateException();
        }
    }

}
