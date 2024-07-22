package net.skykingdoms.backend.votifier.util;

public class QuietException extends Exception {
    public QuietException() {
    }

    public QuietException(String s) {
        super(s);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
