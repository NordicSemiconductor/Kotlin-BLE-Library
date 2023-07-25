package no.nordicsemi.android.kotlin.ble.core.scanner

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.kotlin.ble.core.ServerDevice

/**
 * Class containing all scan results grouped with an advertising device.
 *
 * @property device [ServerDevice] which may be connectable
 * @property data List of scan results ([BleScanResultData]) captured during scanning.
 */
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
