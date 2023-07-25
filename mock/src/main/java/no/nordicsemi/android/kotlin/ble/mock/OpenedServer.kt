package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI

/**
 * Wrapper class grouping server api with it's services.
 *
 * @property serverApi [GattServerAPI] for communication with server side.
 * @property services A services for copy with each connection.
 */
data class OpenedServer(
    val serverApi: GattServerAPI,
    val services: List<IBluetoothGattService>,
)
