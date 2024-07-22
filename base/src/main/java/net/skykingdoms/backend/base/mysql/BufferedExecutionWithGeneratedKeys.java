package net.skykingdoms.backend.base.mysql;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public final class BufferedExecutionWithGeneratedKeys extends BufferedExecution {

    int generatedKey;

    public BufferedExecutionWithGeneratedKeys(final int generatedKey,
                                              final int affectedRows) {
        super(affectedRows);
        this.generatedKey = generatedKey;
    }
}
