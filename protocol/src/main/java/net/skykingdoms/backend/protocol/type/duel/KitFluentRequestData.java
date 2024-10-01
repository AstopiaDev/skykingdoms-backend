package net.skykingdoms.backend.protocol.type.duel;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.skykingdoms.backend.protocol.packet.PacketUtils;

@Accessors(fluent = true)
@Getter
@Setter
public class KitFluentRequestData extends SimpleFluentDuelRequestData {
    String kit = "";

    public KitFluentRequestData(String map, String kit) {
        super(map);
        this.kit = kit;
    }

    public KitFluentRequestData() {
        super("");
    }

    @Override
    public KitFluentRequestData serialize(@NonNull ByteBuf out) {
        super.serialize(out);
        PacketUtils.writeString(out, kit);
        return this;
    }

    @Override
    public KitFluentRequestData deserialize(@NonNull ByteBuf in) {
        super.deserialize(in);
        this.kit = PacketUtils.readString(in);
        return this;
    }
}
