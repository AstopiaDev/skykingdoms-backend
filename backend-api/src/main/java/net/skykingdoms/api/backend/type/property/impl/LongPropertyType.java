package net.skykingdoms.api.backend.type.property.impl;

import io.netty.buffer.ByteBuf;
import net.skykingdoms.api.backend.type.property.PropertyType;
import net.skykingdoms.api.protocol.util.PacketUtils;
import net.skykingdoms.api.protocol.util.TextFormat;

public class LongPropertyType extends PropertyType<Long> {
    @Override
    public Long deserialize(String data) {
        return this.value = Long.parseLong(data);
    }

    @Override
    public void serialize(ByteBuf buf) {
        PacketUtils.writeString(buf, "LONG");
        PacketUtils.writeString(buf, String.valueOf(value));
    }

    @Override
    public String getInsertStatement(int playerId, String key) {
        return TextFormat.formatText("INSERT INTO `LongProperties` (`GamerId`, `Property`, `Value`) VALUES ({}, '{}', {}) ON DUPLICATE KEY UPDATE `Value` = {};",
                playerId,        // 1
                key,            // 2
                value,          // 3
                value);         // 4 (for the ON DUPLICATE KEY UPDATE part)
    }
}
