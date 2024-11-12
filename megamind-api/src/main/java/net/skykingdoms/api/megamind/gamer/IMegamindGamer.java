package net.skykingdoms.api.megamind.gamer;

import lombok.NonNull;
import net.skykingdoms.api.backend.setting.Setting;
import net.skykingdoms.api.backend.setting.SettingType;

import java.time.LocalDateTime;

public interface IMegamindGamer {
    String getDomain();
    LocalDateTime getFirstLogin();
    LocalDateTime getLastLogin();
    boolean isOnline();
    boolean isRegistered();
    boolean isLoggedIn();
    boolean isPremium();
    void quit();
    void setSetting(final @NonNull SettingType settingType, final @NonNull Setting<?, ?> value);
    void updateSetting(final @NonNull SettingType settingType, final @NonNull Setting<?, ?> value);
    void addProperty(@NonNull String property, int value);
    <T> void setProperty(final @NonNull String property, final T value);
    <T> void updateProperty(final @NonNull String key, final T value);
    void removeProperty(final @NonNull String key);
    void dropProperty(final @NonNull String key);
    <T> T getProperty(final @NonNull String key);
    boolean isModern();
    void sendTitle(@NonNull String title, @NonNull String subtitle, int fadeIn, int stay, int fadeOut);
    void disconnect(@NonNull String reason);
    void sendLocalizedMessage(@NonNull String key, Object... objects);
}