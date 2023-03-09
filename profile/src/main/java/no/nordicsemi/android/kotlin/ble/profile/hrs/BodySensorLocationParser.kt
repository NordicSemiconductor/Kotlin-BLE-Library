package no.nordicsemi.android.kotlin.ble.profile.hrs

import no.nordicsemi.android.kotlin.ble.profile.common.Data

object BodySensorLocationParser {

    fun parse(byteArray: ByteArray): Int? {
        val data = Data(byteArray)

        if (data.size() < 1) {
            return null
        }

        return data.getIntValue(Data.FORMAT_UINT8, 0)
    }
}
