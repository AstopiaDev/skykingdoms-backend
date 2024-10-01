package net.skykingdoms.backend.base.command.tabcomplete;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.skykingdoms.api.backend.gamer.ICommandSender;

import java.util.List;

/**
 * @author artem
 */
@RequiredArgsConstructor
public class UnmodifiableTabcompleter extends AbstractTabcompleter {

    private final String[] variants;

    @Override
    public List<String> complete(final @NonNull ICommandSender sender,
                                 final @NonNull String label) {
        return any(label, variants);
    }

}
