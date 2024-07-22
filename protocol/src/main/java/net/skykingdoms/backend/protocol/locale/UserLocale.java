package net.skykingdoms.backend.protocol.locale;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface UserLocale {

    String getName();

    String getMessage(boolean modern, final @NotNull String path, final Object @NotNull ... params);

    List<String> getMessageList(boolean modern, final @NotNull String path, final Object @NotNull ... params);

}
