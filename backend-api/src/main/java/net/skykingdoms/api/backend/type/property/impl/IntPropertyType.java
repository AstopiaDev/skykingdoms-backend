package net.skykingdoms.api.backend.type.property.impl;

import io.netty.buffer.ByteBuf;
import net.skykingdoms.api.backend.type.property.PropertyType;
import net.skykingdoms.api.protocol.util.PacketUtils;
import net.skykingdoms.api.protocol.util.TextFormat;

public class IntPropertyType extends PropertyType<Integer> {
    @Override
    public Integer deserialize(String data) {
        return this.value = Integer.parseInt(data);
    }

    @Override
    public void serialize(ByteBuf buf) {
        PacketUtils.writeString(buf, "INT");
        PacketUtils.writeString(buf, String.valueOf(value));
    }

    @Override
    public String getInsertStatement(int playerId, String key) {
        return TextFormat.formatText("INSERT INTO `IntProperties` (`GamerId`, `Property`, `Value`) VALUES ({}, '{}', {}) ON DUPLICATE KEY UPDATE `Value` = {};",
                playerId,        // 1
                key,            // 2
                value,          // 3
                value);         // 4 (for the ON DUPLICATE KEY UPDATE part)
    }
}
