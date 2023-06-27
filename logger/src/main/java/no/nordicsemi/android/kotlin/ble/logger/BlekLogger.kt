package no.nordicsemi.android.kotlin.ble.logger

import android.content.Context
import android.util.Log

interface BlekLoggerAndLauncher : BlekLauncher, BlekLogger

interface BlekLauncher {
    fun launch()
}

fun interface BlekLogger {

    fun log(priority: Int, log: String)
}

class DefaultBlekLogger(private val context: Context) : BlekLoggerAndLauncher {

    override fun log(priority: Int, log: String) {
        Log.println(priority, "BLEK-LOG", log)
    }

    override fun launch() {
        LoggerLauncher.launch(context)
    }
}
