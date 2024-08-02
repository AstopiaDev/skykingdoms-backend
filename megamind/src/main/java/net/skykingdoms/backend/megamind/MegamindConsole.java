package net.skykingdoms.backend.megamind;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import net.skykingdoms.backend.megamind.api.command.ConsoleCommandSender;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

@RequiredArgsConstructor
public final class MegamindConsole extends SimpleTerminalConsole {
    LineReader lineReader;

    @Override
    protected boolean isRunning() {
        return MegamindApp.INSTANCE.running();
    }

    @Override
    protected void runCommand(final @NonNull String line) {
        MegamindApp.INSTANCE.executeCommand(ConsoleCommandSender.INSTANCE, "/" + line);
        lineReader.printAbove("");
/*        lineReader.callWidget(LineReader.CLEAR);
        lineReader.callWidget(LineReader.REDRAW_LINE);
        lineReader.callWidget(LineReader.REDISPLAY);
        lineReader.getTerminal().writer().flush();*/
    }

    @Override
    protected void shutdown() {
        MegamindApp.INSTANCE.shutdown();
    }

    @Override
    protected LineReader buildReader(final @NonNull LineReaderBuilder builder) {
        lineReader = builder
                .appName("Megamind")
                .completer(new MegamindConsoleCompleter())
                .build();
        return lineReader;
    }

}