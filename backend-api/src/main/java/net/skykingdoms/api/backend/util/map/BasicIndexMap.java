package net.skykingdoms.api.backend.util.map;

import net.skykingdoms.api.backend.util.collection.CollectionUtil;

import java.util.Collections;
import java.util.Set;

public class BasicIndexMap<Instance> extends IndexMap<Instance> {

    private final Set<Instance> entries = CollectionUtil.createConcurrentSet();

    @Override
    public void store(Instance instance) {
        super.store(instance);

        entries.add(instance);
    }

    @Override
    public void remove(Instance instance) {
        super.remove(instance);

        entries.remove(instance);
    }

    @Override
    public Set<Instance> values() {
        return Collections.unmodifiableSet(entries);
    }

    @Override
    public int size() {
        return entries.size();
    }
}
