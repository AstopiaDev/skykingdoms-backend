package net.skykingdoms.backend.protocol.packet;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import lombok.NonNull;

public final class PacketMapper {

    private final PacketFactory[] idFactoryMap = new PacketFactory[256];
    private final TObjectIntMap<Class<?>> classIdMap = new TObjectIntHashMap<>(256, 0.5F, -1);

    public void registerPacket(final int id, final @NonNull Class<?> cls, final @NonNull PacketFactory<?> factory) {
        idFactoryMap[id] = factory;
        classIdMap.put(cls, id);
    }

    public Packet<?> newPacket(int id) {
        if (id >= idFactoryMap.length) return null;

        PacketFactory<?> supplier = idFactoryMap[id];
        if (supplier == null) return null;

        return supplier.newInstance();
    }

    public int getPacketId(final @NonNull Class<?> cls) {
        return classIdMap.get(cls);
    }

    // New method to get the PacketFactory by packet ID
    public PacketFactory<?> getPacketFactory(int id) {
        if (id >= idFactoryMap.length || idFactoryMap[id] == null) {
            return null;
        }
        return idFactoryMap[id];
    }

    // Unregister packet by ID
    public void unregisterPacket(int id) {
        if (id >= 0 && id < idFactoryMap.length) {
            idFactoryMap[id] = null;
        }
    }
}