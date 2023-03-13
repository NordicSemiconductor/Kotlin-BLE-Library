package no.nordicsemi.android.kotlin.ble.profile.battery

import no.nordicsemi.android.kotlin.ble.profile.common.ByteData

object BatteryLevelParser {

    fun parse(bytes: ByteArray): Int? {
        val data = ByteData(bytes)
        if (data.size() == 1) {
            return data.getIntValue(ByteData.FORMAT_UINT8, 0)
        }
        return null
    }
}
