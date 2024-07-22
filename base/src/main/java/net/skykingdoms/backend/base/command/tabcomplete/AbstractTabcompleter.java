package net.skykingdoms.backend.base.command.tabcomplete;

import lombok.NonNull;
import net.skykingdoms.backend.base.util.string.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractTabcompleter implements Tabcompleter {
    public List<String> any(@NonNull String prefix,
                            @NonNull String... variants) {
        return any(prefix, Arrays.stream(variants));
    }

    public List<String> any(@NonNull String prefix,
                            @NonNull Stream<String> variants) {
        return variants.filter(variant -> StringUtil.startsWith(variant, prefix, true))
                .sorted().limit(50).collect(Collectors.toList());
    }
}
