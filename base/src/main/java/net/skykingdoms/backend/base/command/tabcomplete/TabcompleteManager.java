package net.skykingdoms.backend.base.command.tabcomplete;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabcompleteManager {

    public static final TabcompleteManager IMP = new TabcompleteManager();

    private static final Pattern PATTERN = Pattern.compile("\\[[^]]+]", Pattern.DOTALL);

    private final Map<String, Function<String, Tabcompleter>> tabcompleters = new HashMap<>();

    {
        // TODO
        // @Tabcomplete(idx=0, name="any: 1, 2, 3");
        // @Tabcomplete(idx=1, name="group: 1, 2, 3, 4");

        addCompleter("any", data -> new StaticTabcompleter(data.split("\\|")));

/*        addCompleter("group", new UnmodifiableTabcompleter(Arrays.stream(Group.values())
                .map(Group::name).sorted().toArray(String[]::new)));*/
    }

    public void addCompleter(String key, Function<String, Tabcompleter> factory) {
        tabcompleters.put(key, factory);
    }

    public void addCompleter(final @NonNull String key,
                             final @NonNull Tabcompleter completer) {
        addCompleter(key, data -> completer);
    }

    public final Tabcompleter[] parsePattern(final @NonNull String pattern) {
        List<Tabcompleter> result = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(pattern);

        while (matcher.find()) {
            String group = matcher.group();
            String[] params = group.substring(1, group.length() - 1).split(":\\s+", 2);
            String data = params.length > 1 ? params[1] : "";

            Function<String, Tabcompleter> factory = tabcompleters.get(params[0]);

            if (factory == null) {
                throw new IllegalStateException("Unknown tabcompleter for " + params[0]);
            }

            result.add(factory.apply(data));
        }

        return result.toArray(new Tabcompleter[0]);
    }

}
