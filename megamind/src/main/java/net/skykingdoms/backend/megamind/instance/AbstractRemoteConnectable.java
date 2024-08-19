package net.skykingdoms.backend.megamind.instance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.skykingdoms.backend.megamind.connection.RemoteClientConnection;
import net.skykingdoms.backend.protocol.channel.AbstractRemoteClientChannel;
import net.skykingdoms.backend.protocol.server.IConnectable;
import org.jetbrains.annotations.NotNull;


@Getter
@AllArgsConstructor
public abstract class AbstractRemoteConnectable implements IConnectable {

    /**
     * Remote instance's name
     */
    String name;

    /**
     * Connection instance
     */
    RemoteClientConnection connection;

    public void close() {
        connection.close();
    }


    @Override
    public @NotNull AbstractRemoteClientChannel getChannel() {
        return connection;
    }
}
