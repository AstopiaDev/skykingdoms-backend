package net.skykingdoms.backend.base.command.exception;

import lombok.NonNull;
import net.skykingdoms.backend.base.command.execution.ExecutionSession;
import net.skykingdoms.backend.base.gamer.AbstractGamer;
import net.skykingdoms.backend.base.gamer.ICommandSender;
import net.skykingdoms.backend.base.language.Language;
import net.skykingdoms.backend.base.util.string.ObjectLinkedBuf;

import java.util.Collections;
import java.util.List;

public class CommandTranslationException extends CommandMessageException {

    private final ObjectLinkedBuf[] messages;

    public CommandTranslationException(Object... objects) {
        this(new ObjectLinkedBuf(objects));
    }

    public CommandTranslationException(ObjectLinkedBuf... messages) {
        super(Collections.emptyList());

        for (ObjectLinkedBuf message : messages) {
            if (message.size() % 2 != 1) {
                throw new IllegalStateException();
            }
        }

        this.messages = messages;
    }

    public CommandTranslationException(List<ObjectLinkedBuf> messages) {
        this(messages.toArray(new ObjectLinkedBuf[0]));
    }

    @Override
    public void printMessages(final @NonNull ExecutionSession session) {
        printMessages(session.getSender());
    }

    public void printMessages(ICommandSender sender) {
        sender.sendLocalizedMessage(!sender.isConsole() && ((AbstractGamer) sender).isModern(), getMessage(!sender.isConsole() && ((AbstractGamer) sender).isModern(), sender.getLanguage()));
    }

    @Override
    public String getMessage() {
        return getMessage(false, Language.DEFAULT);
    }

    public String getMessage(boolean modern, Language language) {
        StringBuilder builder = new StringBuilder();
        boolean appendNewLine = false;

        for (ObjectLinkedBuf array : messages) {
            if (appendNewLine) {
                builder.append("\n");
            } else {
                appendNewLine = true;
            }

            ObjectLinkedBuf.Reader reader = array.reader();

            String key = reader.readNext();

            Object[] params = new Object[array.size() - 1];
            reader.readNextObjects(params);

            builder.append(language.getMessage(modern, key, params));
        }

        return builder.toString();
    }
}
