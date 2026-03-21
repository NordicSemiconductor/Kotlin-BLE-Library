package no.nordicsemi.kotlin.ble.core.logger

import org.slf4j.LoggerFactory

actual fun createLogger(tag: String): Logger = Slf4jLogger(tag)

private class Slf4jLogger(tag: String) : Logger {
    private val delegate = LoggerFactory.getLogger(tag)

    override fun trace(message: String) { delegate.trace(message) }
    override fun trace(format: String, arg: Any?) { delegate.trace(format, arg) }
    override fun info(message: String) { delegate.info(message) }
    override fun info(format: String, arg: Any?) { delegate.info(format, arg) }
    override fun warn(message: String?) { delegate.warn(message) }
    override fun warn(message: String?, throwable: Throwable) { delegate.warn(message, throwable) }
}
