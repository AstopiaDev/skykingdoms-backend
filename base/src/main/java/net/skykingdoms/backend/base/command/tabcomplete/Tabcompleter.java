package net.skykingdoms.backend.base.command.tabcomplete;

import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ICommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface Tabcompleter {

    @Nullable List<String> complete(final @NotNull ICommandSender sender,
                                    final @NotNull String label);

    default Tabcompleter with(final @NonNull Tabcompleter another) {
        return (sender, label) -> {
            List<String> first = complete(sender, label);
            List<String> second = another.complete(sender, label);

            if (first == null && second == null) {
                return Collections.emptyList();
            }

            List<String> result = new ArrayList<>();

            if (first != null) {
                result.addAll(first);
            }

            if (second != null) {
                result.addAll(second);
            }

            return result.stream().sorted().limit(50)
                    .collect(Collectors.toList());
        };
    }

}
