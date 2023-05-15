package no.nordicsemi.android.kotlin.ble.scanner.data

import no.nordicsemi.android.kotlin.ble.core.ServerDevice

data class AggregatedBleScanItemWithRecord(
    val device: ServerDevice,
    val scanResult: List<BleScanResult> = emptyList()
) {
    val highestRssi = scanResult.maxOf { it.rssi }
    val lastScanResult = scanResult.lastOrNull()
}
