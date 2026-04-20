package no.nordicsemi.kotlin.ble.core.logger

actual class Logger actual constructor(
    private val tag: String,
) {
    actual fun trace(message: String?) {
        print("TRACE", message)
    }

    actual fun trace(format: String, vararg args: Any?) {
        print("TRACE", formatMessage(format, args))
    }

    actual fun trace(message: String?, throwable: Throwable) {
        print("TRACE", message, throwable)
    }

    actual fun debug(message: String?) {
        print("DEBUG", message)
    }

    actual fun debug(format: String, vararg args: Any?) {
        print("DEBUG", formatMessage(format, args))
    }

    actual fun debug(message: String?, throwable: Throwable) {
        print("DEBUG", message, throwable)
    }

    actual fun info(message: String?) {
        print("INFO", message)
    }

    actual fun info(format: String, vararg args: Any?) {
        print("INFO", formatMessage(format, args))
    }

    actual fun info(message: String?, throwable: Throwable) {
        print("INFO", message, throwable)
    }

    actual fun warn(message: String?) {
        print("WARN", message)
    }

    actual fun warn(format: String, vararg args: Any?) {
        print("WARN", formatMessage(format, args))
    }

    actual fun warn(message: String?, throwable: Throwable) {
        print("WARN", message, throwable)
    }

    actual fun error(message: String?) {
        print("ERROR", message)
    }

    actual fun error(format: String, vararg args: Any?) {
        print("ERROR", formatMessage(format, args))
    }

    actual fun error(message: String?, throwable: Throwable) {
        print("ERROR", message, throwable)
    }

    private fun print(level: String, message: String?, throwable: Throwable? = null) {
        val renderedMessage = message ?: "null"
        println("[$tag] $level: $renderedMessage")
        throwable?.printStackTrace()
    }

    private fun formatMessage(format: String, args: Array<out Any?>): String {
        var rendered = format
        args.forEach { arg ->
            rendered = rendered.replaceFirst("{}", arg.toString())
        }
        return rendered
    }
}
