package no.nordicsemi.android.kotlin.ble.profile.hrs

import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.hrs.data.HRSData
import java.util.*

object HRSDataParser {

    fun parse(byteArray: ByteArray): HRSData? {
        val data = ByteData(byteArray)

        if (data.size() < 2) {
            return null
        }

        // Read flags

        // Read flags
        var offset = 0
        val flags: Int = data.getIntValue(ByteData.FORMAT_UINT8, offset) ?: return null
        val hearRateType: Int = if (flags and 0x01 == 0) ByteData.FORMAT_UINT8 else ByteData.FORMAT_UINT16_LE
        val sensorContactStatus = flags and 0x06 shr 1
        val sensorContactSupported = sensorContactStatus == 2 || sensorContactStatus == 3
        val sensorContactDetected = sensorContactStatus == 3
        val energyExpandedPresent = flags and 0x08 != 0
        val rrIntervalsPresent = flags and 0x10 != 0
        offset += 1


        // Validate packet length
        if (data.size() < (1 + (hearRateType and 0x0F) + (if (energyExpandedPresent) 2 else 0) + if (rrIntervalsPresent) 2 else 0)) {
            return null
        }

        // Prepare data

        // Prepare data
        val sensorContact = if (sensorContactSupported) sensorContactDetected else false

        val heartRate: Int = data.getIntValue(hearRateType, offset) ?: return null
        offset += hearRateType and 0xF

        var energyExpanded: Int? = null
        if (energyExpandedPresent) {
            energyExpanded = data.getIntValue(ByteData.FORMAT_UINT16_LE, offset)
            offset += 2
        }

        val rrIntervals = if (rrIntervalsPresent) {
            val count: Int = (data.size() - offset) / 2
            val intervals: MutableList<Int> = ArrayList(count)
            for (i in 0 until count) {
                intervals.add(data.getIntValue(ByteData.FORMAT_UINT16_LE, offset)!!)
                offset += 2
            }
            intervals.toList()
        } else {
            emptyList()
        }

        return HRSData(heartRate, sensorContact, energyExpanded, rrIntervals)
    }
}
