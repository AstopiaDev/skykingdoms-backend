package net.skykingdoms.backend.protocol.packet.play;

import io.netty.buffer.ByteBuf;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.skykingdoms.api.protocol.packet.serialize.Serializable;
import net.skykingdoms.backend.protocol.packet.ChannelProcessorContext;
import net.skykingdoms.backend.protocol.packet.Packet;
import net.skykingdoms.backend.protocol.packet.PacketProcessor;
import net.skykingdoms.backend.protocol.packet.PacketUtils;
import net.skykingdoms.backend.protocol.packet.play.processor.PlayServerProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vihara
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public final class S10RegisterCommands extends Packet<PlayServerProcessor> {

    CommandContainer[] commands;

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
        commands = PacketUtils.readSerializableArray(buf, CommandContainer[]::new, CommandContainer::new);
    }

    @Override
    public void write0(@NotNull ByteBuf buf) throws Exception {
        PacketUtils.writeSerializableArray(buf, commands);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CommandContainer implements Serializable<CommandContainer> {

        Set<String> commands;
        CommandSource source;
        String serverName;

        public static CommandContainer[] coreSingleton(Set<String> coreCommands) {
            return new CommandContainer[] {
                    new CommandContainer(coreCommands, CommandSource.CORE, null)
            };
        }

        public static CommandContainer[] serverSingleton(Set<String> serverCommands, String serverName) {
            return new CommandContainer[] {
                    new CommandContainer(serverCommands, CommandSource.SERVER, serverName)
            };
        }

        @Override
        public CommandContainer serialize(@NotNull ByteBuf out) {
            PacketUtils.writeCollection(out, commands, s -> PacketUtils.writeString(out, s));
            PacketUtils.writeEnum(out, source);
            if (source == CommandSource.SERVER) {
                PacketUtils.writeString(out, serverName);
            }
            return this;
        }

        @Override
        public CommandContainer deserialize(@NotNull ByteBuf in) {
            commands = PacketUtils.readCollection(in, HashSet::new, () -> PacketUtils.readString(in));
            source = PacketUtils.readEnum(in, CommandSource.class);
            if (source == CommandSource.SERVER) {
                serverName = PacketUtils.readString(in);
            }
            return this;
        }
    }

    public enum CommandSource {
        CORE,
        SERVER
    }

}