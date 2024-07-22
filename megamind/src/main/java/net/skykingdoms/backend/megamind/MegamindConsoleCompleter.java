package net.skykingdoms.backend.megamind;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.megamind.api.command.ConsoleCommandSender;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.stream.Collectors;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class MegamindConsoleCompleter implements Completer {

    @Override
    public void complete(final @NonNull LineReader lineReader,
                         final @NonNull ParsedLine parsedLine,
                         final List<@NonNull Candidate> list) {
        var suggests = MegamindApp.INSTANCE.tabComplete(ConsoleCommandSender.INSTANCE, "/" + parsedLine.line());

        if (suggests == null) {
            suggests = MegamindApp.INSTANCE.getCommands().stream().sorted().limit(50)
                    .collect(Collectors.toList());
        }

        for (val suggest : suggests) {
            list.add(new Candidate(suggest));
        }
    }
}