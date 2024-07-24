package net.skykingdoms.backend.protocol.type.duel;

import io.netty.buffer.ByteBuf;
import lombok.NonNull;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

public class SimpleFluentDuelRequestData extends FluentDuelRequestData {
    public SimpleFluentDuelRequestData(String map) {
        super(map);
    }

    public SimpleFluentDuelRequestData() {
        super("");
    }

    @Override
    public FluentDuelRequestData serialize(@NonNull ByteBuf out) {
        PacketUtils.writeString(out, map);
        return this;
    }

    @Override
    public FluentDuelRequestData deserialize(@NonNull ByteBuf in) {
        this.map = PacketUtils.readString(in);
        return this;
    }
}
