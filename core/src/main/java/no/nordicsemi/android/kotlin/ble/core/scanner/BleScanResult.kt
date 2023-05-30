package no.nordicsemi.android.kotlin.ble.core.scanner

import no.nordicsemi.android.kotlin.ble.core.ServerDevice

data class BleScanResult(
    val device: ServerDevice,
    val data: BleScanResultData? = null
)
