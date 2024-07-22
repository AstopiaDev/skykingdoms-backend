package net.skykingdoms.backend.protocol.pipeline.exception;

public final class BadPacketException extends InstantException {

    public BadPacketException(final int packetId) {
        super("Bad Packet: [ID: " + packetId + "]");
    }
}
