package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

@Parcelize
data class BleScanResults(
    val device: ServerDevice,
    val scanResult: List<BleScanResultData> = emptyList()
): Parcelable {

    @IgnoredOnParcel
    val highestRssi = scanResult.maxOfOrNull { it.rssi } ?: 0
    @IgnoredOnParcel
    val lastScanResult = scanResult.lastOrNull()
}
