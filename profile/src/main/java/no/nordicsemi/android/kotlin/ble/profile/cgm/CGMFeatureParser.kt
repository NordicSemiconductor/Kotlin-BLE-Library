package no.nordicsemi.android.kotlin.ble.profile.cgm

import android.annotation.SuppressLint
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMFeatures
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMFeaturesEnvelope
import no.nordicsemi.android.kotlin.ble.profile.common.ByteData
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16
import no.nordicsemi.android.kotlin.ble.profile.common.IntFormat

object CGMFeatureParser {

    fun parse(byteArray: ByteArray): CGMFeaturesEnvelope? {
        val data = ByteData(byteArray)

        if (data.size() != 6) {
            return null
        }

        val featuresValue: Int = data.getIntValue(IntFormat.FORMAT_UINT24_LE, 0) ?: return null
        val typeAndSampleLocation: Int = data.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
        val expectedCrc: Int = data.getIntValue(IntFormat.FORMAT_UINT16_LE, 4) ?: return null

        val features = CGMFeatures(featuresValue)
        if (features.e2eCrcSupported) {
            val actualCrc: Int = CRC16.MCRF4XX(data.value, 0, 4)
            if (actualCrc != expectedCrc) {
                return null
            }
        } else {
            // If the device doesn't support E2E-safety the value of the field shall be set to 0xFFFF.
            if (expectedCrc != 0xFFFF) {
                return null
            }
        }

        @SuppressLint("WrongConstant") val type = typeAndSampleLocation and 0x0F // least significant nibble

        val sampleLocation = typeAndSampleLocation shr 4 // most significant nibble

        return CGMFeaturesEnvelope(features, type, sampleLocation, features.e2eCrcSupported, features.e2eCrcSupported)
    }
}
