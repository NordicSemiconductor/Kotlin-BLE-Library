package no.nordicsemi.android.kotlin.ble.profile.bps

import no.nordicsemi.android.kotlin.ble.profile.bps.data.BPMStatus
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureMeasurementData
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureType
import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.date.DateTimeParser
import java.util.*

object BloodPressureMeasurementParser {

    fun parse(byteArray: ByteArray): BloodPressureMeasurementData? {
        val data = ByteData(byteArray)
        if (data.size() < 7) {
            return null
        }

        // First byte: flags
        var offset = 0
        val flags: Int = data.getIntValue(ByteData.FORMAT_UINT8, offset++) ?: return null

        // See UNIT_* for unit options
        val unit: BloodPressureType = if (flags and 0x01 == BloodPressureType.UNIT_MMHG.value) {
            BloodPressureType.UNIT_MMHG
        } else {
            BloodPressureType.UNIT_KPA
        }
        val timestampPresent = flags and 0x02 != 0
        val pulseRatePresent = flags and 0x04 != 0
        val userIdPresent = flags and 0x08 != 0
        val measurementStatusPresent = flags and 0x10 != 0

        if (data.size() < (7
                    + (if (timestampPresent) 7 else 0) + (if (pulseRatePresent) 2 else 0)
                    + (if (userIdPresent) 1 else 0) + if (measurementStatusPresent) 2 else 0)
        ) {
            return null
        }

        // Following bytes - systolic, diastolic and mean arterial pressure
        val systolic: Float = data.getFloatValue(ByteData.FORMAT_SFLOAT, offset) ?: return null
        val diastolic: Float = data.getFloatValue(ByteData.FORMAT_SFLOAT, offset + 2) ?: return null
        val meanArterialPressure: Float = data.getFloatValue(ByteData.FORMAT_SFLOAT, offset + 4) ?: return null
        offset += 6

        // Parse timestamp if present
        var calendar: Calendar? = null
        if (timestampPresent) {
            calendar = DateTimeParser.parse(data, offset)
            offset += 7
        }

        // Parse pulse rate if present
        var pulseRate: Float? = null
        if (pulseRatePresent) {
            pulseRate = data.getFloatValue(ByteData.FORMAT_SFLOAT, offset)
            offset += 2
        }

        // Read user id if present
        var userId: Int? = null
        if (userIdPresent) {
            userId = data.getIntValue(ByteData.FORMAT_UINT8, offset)
            offset += 1
        }

        // Read measurement status if present
        var status: BPMStatus? = null
        if (measurementStatusPresent) {
            val measurementStatus: Int = data.getIntValue(ByteData.FORMAT_UINT16_LE, offset) ?: return null
            // offset += 2;
            status = BPMStatus(measurementStatus)
        }

        return BloodPressureMeasurementData(
            systolic,
            diastolic,
            meanArterialPressure,
            unit,
            pulseRate,
            userId,
            status,
            calendar
        )
    }
}
