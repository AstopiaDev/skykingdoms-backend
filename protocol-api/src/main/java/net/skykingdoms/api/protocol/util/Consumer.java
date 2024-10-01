package net.skykingdoms.api.protocol.util;

public interface Consumer<T> extends java.util.function.Consumer<T> {

    static <T> Consumer<T> empty(){
        return $ -> { };
    }

}
