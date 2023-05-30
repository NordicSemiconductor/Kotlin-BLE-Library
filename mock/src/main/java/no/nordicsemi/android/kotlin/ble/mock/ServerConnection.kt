package no.nordicsemi.android.kotlin.ble.mock

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI

internal data class ServerConnection(
    val server: ServerDevice,
    val client: ClientDevice,
    val serverApi: GattServerAPI,
    val clientApi: GattClientAPI,
    val services: List<BluetoothGattService>,
    val params: ConnectionParams,
    val enabledNotification: Map<BluetoothGattCharacteristic, Boolean> = mutableMapOf()
)
