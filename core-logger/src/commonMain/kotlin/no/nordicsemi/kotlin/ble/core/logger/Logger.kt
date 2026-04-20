package no.nordicsemi.kotlin.ble.core.logger

expect class Logger(tag: String) {
    fun trace(message: String?)
    fun trace(format: String, vararg args: Any?)
    fun trace(message: String?, throwable: Throwable)

    fun debug(message: String?)
    fun debug(format: String, vararg args: Any?)
    fun debug(message: String?, throwable: Throwable)

    fun info(message: String?)
    fun info(format: String, vararg args: Any?)
    fun info(message: String?, throwable: Throwable)

    fun warn(message: String?)
    fun warn(format: String, vararg args: Any?)
    fun warn(message: String?, throwable: Throwable)

    fun error(message: String?)
    fun error(format: String, vararg args: Any?)
    fun error(message: String?, throwable: Throwable)
}
