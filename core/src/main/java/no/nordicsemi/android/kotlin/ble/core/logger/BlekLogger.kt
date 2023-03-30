package no.nordicsemi.android.kotlin.ble.core.logger

import android.util.Log

interface BlekLogger {

    fun log(priority: Int, log: String)
}

class DefaultBlekLogger : BlekLogger {

    override fun log(priority: Int, log: String) {
        Log.println(priority, "BLEK-LOG", log)
    }
}
