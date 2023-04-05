package no.nordicsemi.android.kotlin.ble.core.data

data class GattConnectionStateWithStatus(
    val state: GattConnectionState,
    val status: BleGattConnectionStatus
)
