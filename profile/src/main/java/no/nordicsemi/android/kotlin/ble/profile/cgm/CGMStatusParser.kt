package no.nordicsemi.android.kotlin.ble.profile.cgm

import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMStatus
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMStatusEnvelope
import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16
import no.nordicsemi.android.kotlin.ble.profile.common.IntFormat

object CGMStatusParser {

    fun parse(byteArray: ByteArray): CGMStatusEnvelope? {
        val data = ByteData(byteArray)

        if (data.size() != 5 && data.size() != 7) {
            return null
        }

        val timeOffset: Int = data.getIntValue(IntFormat.FORMAT_UINT16_LE, 0) ?: return null
        val warningStatus: Int = data.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
        val calibrationTempStatus: Int = data.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
        val sensorStatus: Int = data.getIntValue(IntFormat.FORMAT_UINT8, 4) ?: return null

        val crcPresent = data.size() == 7
        if (crcPresent) {
            val actualCrc: Int = CRC16.MCRF4XX(data.value, 0, 5)
            val expectedCrc: Int = data.getIntValue(IntFormat.FORMAT_UINT16_LE, 5) ?: return null
            if (actualCrc != expectedCrc) {
                return null
            }
        }

        val status = CGMStatus(warningStatus, calibrationTempStatus, sensorStatus)
        return CGMStatusEnvelope(status, timeOffset, crcPresent, crcPresent)
    }
}
