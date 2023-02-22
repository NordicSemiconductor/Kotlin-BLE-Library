package no.nordicsemi.android.kotlin.ble.profile.battery

import no.nordicsemi.android.kotlin.ble.profile.common.Data

object BatteryLevelParser {

    fun parse(bytes: ByteArray): Int? {
        val data = Data(bytes)
        if (data.size() == 1) {
            return data.getIntValue(Data.FORMAT_UINT8, 0)?.let {
                it
            }
        }
        return null
    }
}
