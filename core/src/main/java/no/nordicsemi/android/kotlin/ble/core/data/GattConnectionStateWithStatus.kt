package no.nordicsemi.android.kotlin.ble.core.data

/**
 * Wrapper class grouping [GattConnectionState] and [BleGattConnectionStatus].
 *
 * @property state If device is connected/disconnected etc.
 * @property status Status of the operation. Can contain additional error code for disconnection.
 */
data class GattConnectionStateWithStatus(
    val state: GattConnectionState,
    val status: BleGattConnectionStatus
)
