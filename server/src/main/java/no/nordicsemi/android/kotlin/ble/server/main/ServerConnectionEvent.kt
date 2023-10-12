package no.nordicsemi.android.kotlin.ble.server.main

import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection

sealed interface ServerConnectionEvent {
    data class DeviceConnected(val connection : ServerBluetoothGattConnection): ServerConnectionEvent
    data class DeviceDisconnected(val device: ClientDevice): ServerConnectionEvent
}