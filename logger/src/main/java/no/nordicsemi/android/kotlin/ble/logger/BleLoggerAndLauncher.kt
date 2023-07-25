package no.nordicsemi.android.kotlin.ble.logger

import android.content.Context
import android.util.Log

/**
 * Interface grouping [BleRemoteLoggerLauncher] and [BleLogger]. Needed to be used as a return type.
 */
interface BleLoggerAndLauncher : BleRemoteLoggerLauncher, BleLogger

/**
 * Functional interface responsible for launching a dedicated logger app.
 */
fun interface BleRemoteLoggerLauncher {

    /**
     * Launches dedicated logger app.
     */
    fun launch()
}

/**
 * Functional interface which defines logs logging action.
 */
fun interface BleLogger {

    /**
     * Prints log.
     *
     * @param priority Priority of the logs.
     * @param log Message.
     */
    fun log(priority: Int, log: String)
}

/**
 * Default implementation of [BleLoggerAndLauncher] which print logs to Logcat and launch main page
 * of [the Logger app](https://play.google.com/store/apps/details?id=no.nordicsemi.android.log&hl=en&gl=US).
 *
 * @property context An application context.
 */
class DefaultBlekLogger(private val context: Context) : BleLoggerAndLauncher {

    override fun log(priority: Int, log: String) {
        Log.println(priority, "BLEK-LOG", log)
    }

    override fun launch() {
        LoggerLauncher.launch(context)
    }
}
