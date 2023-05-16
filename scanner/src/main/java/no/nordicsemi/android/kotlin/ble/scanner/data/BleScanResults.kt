package no.nordicsemi.android.kotlin.ble.scanner.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

@Parcelize
data class BleScanResults(
    val device: ServerDevice,
    val scanResult: List<BleScanResultData> = emptyList()
): Parcelable {
    val highestRssi = scanResult.maxOfOrNull { it.rssi } ?: 0
    val lastScanResult = scanResult.lastOrNull()
}
