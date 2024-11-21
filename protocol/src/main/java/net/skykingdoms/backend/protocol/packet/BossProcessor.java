package net.skykingdoms.backend.protocol.packet;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BossProcessor implements PacketProcessor, Iterable<PacketProcessor> {

    ProcNode first;
    ProcNode last;

    @Override
    public final @NotNull Iterator<PacketProcessor> iterator() {
        return new PacketProcessorIterator(first);
    }

    public final void callActive() throws Exception {
        active();

        for (val processor : this) {
            processor.active();
        }
    }

    public final void callException(final @NonNull Throwable cause) throws Exception {
        process(cause);

        for (val processor : this) {
            processor.process(cause);
        }
    }

    public final void callInactive() throws Exception {
        inactive();

        for (val processor : this) {
            processor.inactive();
        }
    }

    public final void callProcessor(final @NonNull ChannelProcessorContext ctx) throws Exception {
        val packet = ctx.getPacket();

        if (packet.isProcessor(this)) {
            process(ctx);
        }

        for (val processor : this) {
            if (packet.isProcessor(processor)) {
                processor.process(ctx);
            }
        }
    }

    public final void registerProcessor(final @NonNull PacketProcessor processor) {
        val node = new ProcNode(processor);

        if (last == null) {
            first = node;
        } else {
            last.next = node;
            node.prev = last;
        }

        last = node;
    }

    static class PacketProcessorIterator implements Iterator<PacketProcessor> {

        final ProcNodeIterator iterator;

        public PacketProcessorIterator(final ProcNode node) {
            this.iterator = new ProcNodeIterator(node);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public PacketProcessor next() {
            return iterator.next().processor;
        }

    }

    static class ProcNodeIterator implements Iterator<ProcNode> {
        final ProcNode head;
        ProcNode cur;

        public ProcNodeIterator(ProcNode node) {
            this.head = node;
        }

        @Override
        public boolean hasNext() {
            return cur == null ? head != null : cur.next != null;
        }

        @Override
        public ProcNode next() {
            if (cur == null) {
                cur = head;
            } else {
                cur = cur.next;
            }

            return cur;
        }

    }

    static class ProcNode {

        ProcNode next;
        ProcNode prev;

        final PacketProcessor processor;

        public ProcNode(final PacketProcessor processor) {
            this.processor = processor;
        }

    }

}
