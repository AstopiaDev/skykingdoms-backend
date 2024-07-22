package net.skykingdoms.backend.base.util.paged;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.val;
import net.skykingdoms.backend.base.util.math.MathUtil;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class AbstractPaging<Backend> {

    @Getter
    int limit;
    Backend backend;

    public AbstractPaging(Backend backend, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be more than zero");
        }

        this.backend = backend;
        this.limit = limit;
    }

    public int getPages() {
        return MathUtil.divideCeil(size(), limit);
    }

    public boolean hasAnyPages() {
        return size() > 0;
    }

    public Backend getPage(final int index) {
        if (index < 0) {
            return hasAnyPages() ? getPage(0) : null;
        }

        val size = size();
        val pageStartIdx = index * limit;

        if (pageStartIdx >= size) {
            return hasAnyPages() ? getPage(getPages() - 1) : null;
        }

        val pageEndIdx = Math.min((index + 1) * limit, size);

        return subList(pageStartIdx, pageEndIdx);
    }

    public abstract Backend subList(int start, int end);

    public abstract int size();

}
