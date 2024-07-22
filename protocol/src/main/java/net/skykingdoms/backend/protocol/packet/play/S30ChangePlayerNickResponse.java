package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.backend.protocol.locale.UserLocale;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;

@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class S30ChangePlayerNickResponse extends Packet.Response<PlayServerProcessor> {

    ChangeNickStatus status;

    int nickId;
    String name;
    String skinName;

    public S30ChangePlayerNickResponse(final @NonNull ChangeNickStatus status) {
        this(status, 0, null, null);
    }

    @Override
    public void process(@NotNull PlayServerProcessor processor, @NotNull ChannelProcessorContext ctx) throws Exception {
        processor.process(this, ctx);
    }

    @Override
    public boolean isProcessor(PacketProcessor processor) {
        return processor instanceof PlayServerProcessor;
    }

    @Override
    public void read0(@NotNull ByteBuf buf) throws Exception {
        status = PacketUtils.readEnum(buf, ChangeNickStatus.class);

        if (status == ChangeNickStatus.SUCCESS) {
            nickId = PacketUtils.readVarInt(buf);
            name = PacketUtils.readString(buf);
            skinName = PacketUtils.readString(buf);
        }
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeEnum(buf, status);

        if (status == ChangeNickStatus.SUCCESS) {
            PacketUtils.writeVarInt(buf, nickId);
            PacketUtils.writeString(buf, name);
            PacketUtils.writeString(buf, skinName);
        }
    }

    public enum ChangeNickStatus {

        USER_NOT_FOUND,
        ALREADY_USED,
        ALREADY_REGISTERED,
        SUCCESS;

        public String getErrorText(@NonNull UserLocale language) {
            return this == SUCCESS ? null : language.getMessage(false, "nick_error_" + name());
        }

    }

}
