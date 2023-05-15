package no.nordicsemi.android.kotlin.ble.scanner.data

import no.nordicsemi.android.kotlin.ble.core.ServerDevice

data class BleScanItem(
    val device: ServerDevice,
    val scanRecord: BleScanResult? = null
)
