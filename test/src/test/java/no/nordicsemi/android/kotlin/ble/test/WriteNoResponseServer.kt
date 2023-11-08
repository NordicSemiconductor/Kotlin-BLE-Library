package no.nordicsemi.android.kotlin.ble.test

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBluetoothGattConnection
import javax.inject.Inject

class WriteNoResponseServer @Inject constructor(
    private val scope: CoroutineScope
) {

    lateinit var server: ServerBleGatt

    lateinit var firstCharacteristic: ServerBleGattCharacteristic

    fun start(
        context: Context,
        device: MockServerDevice = MockServerDevice(
            name = "Reliable Write Server",
            address = "55:44:33:22:11"
        ),
    ) = scope.launch {
        val firstCharacteristic = ServerBleGattCharacteristicConfig(
            FIRST_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE, BleGattProperty.PROPERTY_READ),
            listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ)
        )

        val serviceConfig = ServerBleGattServiceConfig(
            RELIABLE_WRITE_SERVICE,
            ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            listOf(firstCharacteristic)
        )

        server = ServerBleGatt.create(
            context = context,
            config = arrayOf(serviceConfig),
            mock = device,
            scope = scope
        )

        val advertiser = BleAdvertiser.create(context)
        advertiser.advertise(config = BleAdvertisingConfig(), mock = device).launchIn(scope)

        launch {
            server.connections
                .mapNotNull { it.values.firstOrNull() }
                .collect { setUpConnection(it) }
        }
    }

    internal fun stopServer() {
        server.stopServer()
    }

    private fun setUpConnection(connection: ServerBluetoothGattConnection) {
        val glsService = connection.services.findService(RELIABLE_WRITE_SERVICE)!!
        firstCharacteristic = glsService.findCharacteristic(FIRST_CHARACTERISTIC)!!
    }
}