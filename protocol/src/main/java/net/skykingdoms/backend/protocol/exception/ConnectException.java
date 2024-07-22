package net.skykingdoms.backend.protocol.exception;

import net.skykingdoms.backend.protocol.channel.AbstractChannel;

public class ConnectException extends Exception {

    public ConnectException(AbstractChannel channel, Throwable cause) {
        super("Unable to connect to server [" + channel.getSocketAddress() + "]", cause);
    }

}
