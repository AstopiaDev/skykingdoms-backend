package net.skykingdoms.backend.minestom.logging

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.skykingdoms.api.protocol.logging.Logging
import net.skykingdoms.impl.Impl

@Impl(Logging::class)
class LoggingImpl : Logging() {

    private val logger: ComponentLogger = MinecraftServer.LOGGER
    private val debug: Boolean = System.getProperty("core.debug").toBoolean()

    override fun debug(message: String) {
        if (debug) {
            info(message)
        }
    }

    override fun info(message: String) {
        logger.info(message)
    }

    override fun warn(message: String) {
        logger.warn(message)
    }

    override fun error(message: String) {
        logger.error(message)
    }

    override fun stacktrace(t: Throwable, message: String) {
        logger.trace(message, t)
    }
}
