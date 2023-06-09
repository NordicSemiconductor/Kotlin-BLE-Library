package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI

internal data class ServerConnection(
    val server: ServerDevice,
    val client: ClientDevice,
    val serverApi: GattServerAPI,
    val clientApi: GattClientAPI,
    val services: List<IBluetoothGattService>,
    val params: ConnectionParams,
    val enabledNotification: Map<IBluetoothGattCharacteristic, Boolean> = mutableMapOf()
)
