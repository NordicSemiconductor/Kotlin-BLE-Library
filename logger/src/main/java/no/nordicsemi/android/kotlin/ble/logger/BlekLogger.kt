package no.nordicsemi.android.kotlin.ble.logger

import android.content.Context
import android.util.Log

/**
 * Interface grouping [BlekLauncher] and [BlekLogger]. Needed to be used as a return type.
 *
 */
interface BlekLoggerAndLauncher : BlekLauncher, BlekLogger

/**
 * Functional interface responsible for launching a dedicated logger app.
 */
fun interface BlekLauncher {

    /**
     * Launches dedicated logger app.
     */
    fun launch()
}

/**
 * Functional interface which defines logs logging action.
 */
fun interface BlekLogger {

    /**
     * Prints log.
     *
     * @param priority Priority of the logs.
     * @param log Message.
     */
    fun log(priority: Int, log: String)
}

/**
 * Default implementation of [BlekLoggerAndLauncher] which print logs to Logcat and launch main page
 * of [the Logger app](https://play.google.com/store/apps/details?id=no.nordicsemi.android.log&hl=en&gl=US).
 *
 * @property context
 */
class DefaultBlekLogger(private val context: Context) : BlekLoggerAndLauncher {

    override fun log(priority: Int, log: String) {
        Log.println(priority, "BLEK-LOG", log)
    }

    override fun launch() {
        LoggerLauncher.launch(context)
    }
}
