package no.nordicsemi.android.kotlin.ble.profile.hrs

import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.common.IntFormat

object BodySensorLocationParser {

    fun parse(byteArray: ByteArray): Int? {
        val data = ByteData(byteArray)

        if (data.size() < 1) {
            return null
        }

        return data.getIntValue(IntFormat.FORMAT_UINT8, 0)
    }
}
