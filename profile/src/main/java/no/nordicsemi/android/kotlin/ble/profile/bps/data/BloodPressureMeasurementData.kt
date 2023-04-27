package no.nordicsemi.android.kotlin.ble.profile.bps.data

import java.util.Calendar

data class BloodPressureMeasurementData(
    val systolic: Float,
    val diastolic: Float,
    val meanArterialPressure: Float,
    val unit: BloodPressureType,
    val pulseRate: Float?,
    val userID: Int?,
    val status: BPMStatus?,
    val calendar: Calendar?
)
