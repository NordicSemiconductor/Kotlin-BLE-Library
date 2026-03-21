package no.nordicsemi.kotlin.ble.core.logger

import platform.Foundation.NSLog

actual fun createLogger(tag: String): Logger = IosLogger(tag)

private class IosLogger(private val tag: String) : Logger {
    override fun trace(message: String) { NSLog("[$tag] TRACE: $message") }
    override fun trace(format: String, arg: Any?) { NSLog("[$tag] TRACE: ${format.replace("{}", arg.toString())}") }
    override fun info(message: String) { NSLog("[$tag] INFO: $message") }
    override fun info(format: String, arg: Any?) { NSLog("[$tag] INFO: ${format.replace("{}", arg.toString())}") }
    override fun warn(message: String?) { NSLog("[$tag] WARN: $message") }
    override fun warn(message: String?, throwable: Throwable) { NSLog("[$tag] WARN: $message - ${throwable.message}") }
}
