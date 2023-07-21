package no.nordicsemi.android.kotlin.ble.mock

import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import java.util.UUID

/**
 * Helper class grouping all needed information related to a client - server connection.
 * It allows to react on BLE events in a handy manner.
 *
 * @property server A server device.
 * @property client A client device.
 * @property serverApi API for communication with server.
 * @property clientApi API for communication with client.
 * @property services Services of this GATT server.
 * @property params BLE connection parameters.
 * @property enabledNotification Notification enabled status per characteristic UUID.
 * @property isReliableWriteOn Flag indicating if reliable write has been initiated.
 */
internal data class ServerConnection(
    val server: ServerDevice,
    val client: ClientDevice,
    val serverApi: GattServerAPI,
    val clientApi: GattClientAPI,
    val services: List<IBluetoothGattService>,
    val params: ConnectionParams,
    val enabledNotification: Map<UUID, Boolean> = mutableMapOf(),
    val isReliableWriteOn: Boolean = false
)
