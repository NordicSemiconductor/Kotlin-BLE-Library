package no.nordicsemi.android.kotlin.ble.profile.battery

import no.nordicsemi.android.kotlin.ble.profile.common.Data
import no.nordicsemi.android.kotlin.ble.profile.error.InvalidDataReceived

object BatteryLevelParser {

    fun parse(bytes: ByteArray): Int {
        val data = Data(bytes)
        if (data.size() == 1) {
            data.getIntValue(Data.FORMAT_UINT8, 0)?.let {
                return it
            } ?: throw InvalidDataReceived()
        }
        throw InvalidDataReceived()
    }
}
