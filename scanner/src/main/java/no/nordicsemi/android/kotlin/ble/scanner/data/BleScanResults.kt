package no.nordicsemi.android.kotlin.ble.scanner.data

import no.nordicsemi.android.kotlin.ble.core.ServerDevice

data class BleScanResults(
    val device: ServerDevice,
    val scanResult: List<BleScanResultData> = emptyList()
) {
    val highestRssi = scanResult.maxOfOrNull { it.rssi } ?: 0
    val lastScanResult = scanResult.lastOrNull()
}
