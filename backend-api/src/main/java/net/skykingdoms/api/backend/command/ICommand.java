package net.skykingdoms.api.backend.command;

import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICommand {

    String getName();

    String[] getAliases();

    void execute(final @NonNull ICommandSender sender,
                 final String @NotNull [] args);

    List<String> tabcomplete(final @NotNull ICommandSender sender,
                             final String @NotNull [] args);

}
