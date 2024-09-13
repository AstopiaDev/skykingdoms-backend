package net.skykingdoms.minimind.social;

import lombok.Getter;
import lombok.NonNull;
import net.skykingdoms.backend.base.data.settings.Setting;
import net.skykingdoms.backend.base.data.settings.SettingType;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.IGamer;
import net.skykingdoms.backend.protocol.type.PropertyType;
import net.skykingdoms.minimind.command.CommandSender;
import net.skykingdoms.minimind.social.party.Party;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SocialGamer extends AbstractGamer implements CommandSender, IGamer {
    @Getter
    Party party;

    public SocialGamer(@NonNull UUID uuid) {
        super(uuid);
    }

    @Override
    public void setSetting(@NonNull SettingType settingType, @NonNull Setting<?, ?> value) {

    }

    @Override
    public void sendTitle(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {

    }

    @Override
    public void disconnect(@NotNull String reason) {

    }

    @Override
    public void setProperty(@NonNull PropertyType property, int value) {

    }

    @Override
    public void removeProperty(@NonNull PropertyType key) {

    }

    @Override
    public void dropProperty(@NonNull PropertyType key) {

    }

    @Override
    public void addProperty(@NonNull PropertyType property, int value) {

    }

    @Override
    public void sendMessage(String message) {

    }

    @Override
    public void dispatchCommand(String command) {

    }
}
