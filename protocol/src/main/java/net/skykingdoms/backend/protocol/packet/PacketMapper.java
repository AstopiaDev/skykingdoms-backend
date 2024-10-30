package net.skykingdoms.backend.protocol.packet;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;
import lombok.NonNull;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class PacketMapper {
    private final PacketFactory<?>[] idFactoryMap = new PacketFactory[256];
    private final TObjectIntMap<Class<?>> classIdMap = new TObjectIntHashMap<>(256, 0.5F, -1);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void registerPacket(final int id, final @NonNull Class<?> cls, final @NonNull PacketFactory<?> factory) {
        lock.writeLock().lock();
        try {
            if (id < 0 || id >= idFactoryMap.length) {
                throw new IllegalArgumentException("Packet ID out of bounds: " + id + ": " + cls.getName());
            }
            idFactoryMap[id] = factory;
            classIdMap.put(cls, id);

        } finally {
            lock.writeLock().unlock();
        }
    }

    public Packet<?> newPacket(int id) {
        lock.readLock().lock();
        try {
            if (id < 0 || id >= idFactoryMap.length) {
                return null;
            }
            PacketFactory<?> factory = idFactoryMap[id];
            if (factory == null) {
                return null;
            }
            return factory.newInstance();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getPacketId(final @NonNull Class<?> cls) {
        lock.readLock().lock();
        try {
            int id = classIdMap.get(cls);
            if (id == -1) {

            }
            return id;
        } finally {
            lock.readLock().unlock();
        }
    }

    public PacketFactory<?> getPacketFactory(int id) {
        lock.readLock().lock();
        try {
            if (id < 0 || id >= idFactoryMap.length) {
                return null;
            }
            return idFactoryMap[id];
        } finally {
            lock.readLock().unlock();
        }
    }
}
