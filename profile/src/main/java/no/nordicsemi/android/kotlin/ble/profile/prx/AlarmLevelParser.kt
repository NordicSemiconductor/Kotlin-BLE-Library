package no.nordicsemi.android.kotlin.ble.profile.prx

import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.common.IntFormat

object AlarmLevelParser {

    fun parse(byteArray: ByteArray): AlarmLevel? {
        val data = ByteData(byteArray)

        if (data.size() == 1) {
            val level: Int = data.getIntValue(IntFormat.FORMAT_UINT8, 0) ?: return null
            return AlarmLevel.create(level)
        }
        return null
    }
}
