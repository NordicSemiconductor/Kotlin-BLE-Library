package no.nordicsemi.android.kotlin.ble.mock

import android.bluetooth.BluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI

data class OpenedServer(
    val serverApi: GattServerAPI,
    val services: List<BluetoothGattService>,
)
