package no.nordicsemi.android.kotlin.ble.test

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertiseConfig
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.main.BleGattServer
import no.nordicsemi.android.kotlin.ble.server.main.service.BleGattServerServiceType
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BleServerGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.BluetoothGattServerConnection
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

val RELIABLE_WRITE_SERVICE: UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")

val FIRST_CHARACTERISTIC = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb")
val SECOND_CHARACTERISTIC = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb")

@SuppressLint("MissingPermission")
@Singleton
class ReliableWriteServer @Inject constructor(
    private val scope: CoroutineScope
) {

    lateinit var server: BleGattServer

    lateinit var firstCharacteristic: BleServerGattCharacteristic
    lateinit var secondCharacteristic: BleServerGattCharacteristic

    fun start(
        context: Context,
        device: MockServerDevice = MockServerDevice(
            name = "Reliable Write Server",
            address = "55:44:33:22:11"
        ),
    ) = scope.launch {
        val firstCharacteristic = BleServerGattCharacteristicConfig(
            FIRST_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ)
        )

        val secondCharacteristic = BleServerGattCharacteristicConfig(
            SECOND_CHARACTERISTIC,
            listOf(BleGattProperty.PROPERTY_WRITE, BleGattProperty.PROPERTY_READ),
            listOf(BleGattPermission.PERMISSION_WRITE, BleGattPermission.PERMISSION_READ)
        )

        val serviceConfig = BleServerGattServiceConfig(
            RELIABLE_WRITE_SERVICE,
            BleGattServerServiceType.SERVICE_TYPE_PRIMARY,
            listOf(firstCharacteristic, secondCharacteristic)
        )

        server = BleGattServer.create(
            context = context,
            config = arrayOf(serviceConfig),
            mock = device
        )

        val advertiser = BleAdvertiser.create(context)
        advertiser.advertise(config = BleAdvertiseConfig(), mock = device).launchIn(scope)

        launch {
            server.connections
                .mapNotNull { it.values.firstOrNull() }
                .collect { setUpConnection(it) }
        }
    }

    internal fun stopServer() {
        server.stopServer()
    }

    private fun setUpConnection(connection: BluetoothGattServerConnection) {
        val glsService = connection.services.findService(RELIABLE_WRITE_SERVICE)!!
        firstCharacteristic = glsService.findCharacteristic(FIRST_CHARACTERISTIC)!!
        secondCharacteristic = glsService.findCharacteristic(SECOND_CHARACTERISTIC)!!
    }
}