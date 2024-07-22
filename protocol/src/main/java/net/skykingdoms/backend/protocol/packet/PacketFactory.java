package net.skykingdoms.backend.protocol.packet;

import org.jetbrains.annotations.NotNull;


public interface PacketFactory<T extends PacketProcessor> {

    @NotNull Packet<T> newInstance();

}
