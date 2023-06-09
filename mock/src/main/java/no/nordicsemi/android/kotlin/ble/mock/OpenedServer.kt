package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI

data class OpenedServer(
    val serverApi: GattServerAPI,
    val services: List<IBluetoothGattService>,
)
