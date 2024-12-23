package net.skykingdoms.backend.minestom.event

import net.minestom.server.event.Event
import net.skykingdoms.backend.protocol.type.reconnect.impl.FluentBukkitDataImpl

class InstanceDataUpdateEvent(private val data: FluentBukkitDataImpl) : Event {
    fun data() : FluentBukkitDataImpl {
        return data
    }
}