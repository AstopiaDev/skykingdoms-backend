package net.skykingdoms.backend.protocol.packet;

import lombok.NonNull;

public enum PacketDirection {

    TO_SERVER {
        @Override
        public PacketMapper getMapper(final @NonNull PacketProtocol protocol) {
            return protocol.TO_SERVER;
        }
    },
    TO_CLIENT {
        @Override
        public PacketMapper getMapper(final @NonNull PacketProtocol protocol) {
            return protocol.TO_CLIENT;
        }
    };

    public abstract PacketMapper getMapper(final @NonNull PacketProtocol protocol);
}
