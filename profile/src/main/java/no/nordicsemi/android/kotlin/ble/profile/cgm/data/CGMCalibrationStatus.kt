package no.nordicsemi.android.kotlin.ble.profile.cgm.data

class CGMCalibrationStatus(val value: Int) {
    val rejected: Boolean
    val dataOutOfRange: Boolean
    val processPending: Boolean

    init {
        rejected = value and 0x01 != 0
        dataOutOfRange = value and 0x02 != 0
        processPending = value and 0x04 != 0
    }
}