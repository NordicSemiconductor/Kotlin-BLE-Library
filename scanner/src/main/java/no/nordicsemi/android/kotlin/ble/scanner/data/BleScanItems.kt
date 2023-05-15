package no.nordicsemi.android.kotlin.ble.scanner.data

import no.nordicsemi.android.kotlin.ble.core.ServerDevice

data class BleScanItems(
    val device: ServerDevice,
    val scanRecords: List<BleScanResult> = emptyList()
) {
    val highestRssi = scanRecords.maxOf { it.rssi }
    val lastScanRecord = scanRecords.lastOrNull()
}
