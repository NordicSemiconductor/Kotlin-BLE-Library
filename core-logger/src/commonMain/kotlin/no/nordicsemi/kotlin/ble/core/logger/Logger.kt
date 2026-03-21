package no.nordicsemi.kotlin.ble.core.logger

expect fun createLogger(tag: String): Logger

interface Logger {
    fun trace(message: String)
    fun trace(format: String, arg: Any?)
    fun info(message: String)
    fun info(format: String, arg: Any?)
    fun warn(message: String?)
    fun warn(message: String?, throwable: Throwable)
}
