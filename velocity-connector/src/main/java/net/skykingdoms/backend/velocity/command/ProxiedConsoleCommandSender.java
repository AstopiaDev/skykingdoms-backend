package net.skykingdoms.backend.velocity.command;

import com.velocitypowered.api.command.CommandSource;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import net.skykingdoms.backend.base.language.Language;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vihara
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public final class ProxiedConsoleCommandSender implements ProxiedCommandSender {

    CommandSource commandSender;

    // todo: might need to parse the raw msg

    @Override
    public void sendRawMessage(final @NonNull String message) {
        commandSender.sendMessage(Component.text(message));
    }

    @Override
    public String getDisplayName() {
        return "Console";
    }

    @Override @NotNull
    public Language getLanguage() {
        return Language.DEFAULT;
    }

    @Override
    public boolean isConsole() {
        return true;
    }


}
