package net.skykingdoms.backend.base.command.tabcomplete;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import net.skykingdoms.api.backend.gamer.ICommandSender;

import java.util.Arrays;
import java.util.List;

public class StaticTabcompleter extends AbstractTabcompleter {

    private final String[] variants;

    public StaticTabcompleter(String... variants) {
        Preconditions.checkNotNull(variants, "variants is null");
        Preconditions.checkState(variants.length != 0, "variants is empty");

        this.variants = variants;
    }

    @Override
    public List<String> complete(final @NonNull ICommandSender sender,
                                 final @NonNull String label) {
        return any(label, Arrays.stream(variants));
    }

}
