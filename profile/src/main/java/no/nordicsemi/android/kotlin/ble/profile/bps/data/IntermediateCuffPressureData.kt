package no.nordicsemi.android.kotlin.ble.profile.bps.data

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import java.util.*

data class IntermediateCuffPressureData(
    @FloatRange(from = 0.0) val cuffPressure: Float,
    val unit: BloodPressureType,
    @FloatRange(from = 0.0) val pulseRate: Float? = null,
    @IntRange(from = 0, to = 255) val userID: Int? = null,
    val status: BPMStatus? = null,
    val calendar: Calendar? = null
)