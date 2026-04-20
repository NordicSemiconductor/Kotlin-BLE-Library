package no.nordicsemi.kotlin.ble.core.logger

import org.slf4j.LoggerFactory

actual class Logger actual constructor(tag: String) {
    private val delegate = LoggerFactory.getLogger(tag)

    actual fun trace(message: String?) {
        delegate.trace(message)
    }

    actual fun trace(format: String, vararg args: Any?) {
        delegate.trace(format, *args)
    }

    actual fun trace(message: String?, throwable: Throwable) {
        delegate.trace(message, throwable)
    }

    actual fun debug(message: String?) {
        delegate.debug(message)
    }

    actual fun debug(format: String, vararg args: Any?) {
        delegate.debug(format, *args)
    }

    actual fun debug(message: String?, throwable: Throwable) {
        delegate.debug(message, throwable)
    }

    actual fun info(message: String?) {
        delegate.info(message)
    }

    actual fun info(format: String, vararg args: Any?) {
        delegate.info(format, *args)
    }

    actual fun info(message: String?, throwable: Throwable) {
        delegate.info(message, throwable)
    }

    actual fun warn(message: String?) {
        delegate.warn(message)
    }

    actual fun warn(format: String, vararg args: Any?) {
        delegate.warn(format, *args)
    }

    actual fun warn(message: String?, throwable: Throwable) {
        delegate.warn(message, throwable)
    }

    actual fun error(message: String?) {
        delegate.error(message)
    }

    actual fun error(format: String, vararg args: Any?) {
        delegate.error(format, *args)
    }

    actual fun error(message: String?, throwable: Throwable) {
        delegate.error(message, throwable)
    }
}
