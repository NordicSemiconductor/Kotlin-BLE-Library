package no.nordicsemi.android.kotlin.ble.core.scanner

import no.nordicsemi.android.kotlin.ble.core.ServerDevice

/**
 * Class containing a scan result grouped with an advertising device.
 *
 * @property device [ServerDevice] which may be connectable
 * @property data Single scan result ([BleScanResultData]) captured during scanning.
 */
data class BleScanResult(
    val device: ServerDevice,
    val data: BleScanResultData? = null
)
