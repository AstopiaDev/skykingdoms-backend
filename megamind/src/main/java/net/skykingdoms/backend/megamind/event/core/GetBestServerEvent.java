package net.skykingdoms.backend.megamind.event.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.skykingdoms.backend.megamind.api.plugin.Event;
import net.skykingdoms.backend.megamind.instance.AbstractPlayerHolder;

import java.net.InetAddress;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Setter
@Getter
public class GetBestServerEvent extends Event {

    String name;
    InetAddress address;

    @NonFinal
    AbstractPlayerHolder bestServer;

}
