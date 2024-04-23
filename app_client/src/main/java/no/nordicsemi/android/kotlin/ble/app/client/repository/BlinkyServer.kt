package no.nordicsemi.android.kotlin.ble.app.client.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.advertiser.BleAdvertiser
import no.nordicsemi.android.kotlin.ble.advertiser.callback.OnAdvertisingSetStarted
import no.nordicsemi.android.kotlin.ble.advertiser.callback.OnAdvertisingSetStopped
import no.nordicsemi.android.kotlin.ble.app.client.screen.viewmodel.BlinkySpecifications
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingConfig
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingData
import no.nordicsemi.android.kotlin.ble.core.advertiser.BleAdvertisingSettings
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.server.main.ServerBleGatt
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattCharacteristicConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceConfig
import no.nordicsemi.android.kotlin.ble.server.main.service.ServerBleGattServiceType
import javax.inject.Inject

private const val TAG = "Blinky server"

@SuppressLint("MissingPermission")
class BlinkyServer @Inject constructor(
    private val scope: CoroutineScope,
) {

    private val buttonState = MutableStateFlow(DataByteArray.from(0x00))

    fun start(context: Context) = scope.launch {
        val ledCharacteristic = ServerBleGattCharacteristicConfig(
            BlinkySpecifications.UUID_LED_CHAR,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_WRITE),
            listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE),
            initialValue = DataByteArray.from(0x01)
        )

        val buttonCharacteristic = ServerBleGattCharacteristicConfig(
            BlinkySpecifications.UUID_BUTTON_CHAR,
            listOf(BleGattProperty.PROPERTY_READ, BleGattProperty.PROPERTY_NOTIFY),
            listOf(BleGattPermission.PERMISSION_READ, BleGattPermission.PERMISSION_WRITE)
        )

        val serviceConfig = ServerBleGattServiceConfig(
            BlinkySpecifications.UUID_SERVICE_DEVICE,
            ServerBleGattServiceType.SERVICE_TYPE_PRIMARY,
            listOf(ledCharacteristic, buttonCharacteristic)
        )

        val advertiser = BleAdvertiser.create(context)
        val advertiserConfig = BleAdvertisingConfig(
            settings = BleAdvertisingSettings(
                deviceName = "nRF Blinky" // Advertise a device name
            ),
            advertiseData = BleAdvertisingData(
                ParcelUuid(BlinkySpecifications.UUID_SERVICE_DEVICE) //Advertise main service uuid.
            )
        )

        val mockDevice = MockServerDevice()

        advertiser.advertise(advertiserConfig, mockDevice) //Start advertising
            .cancellable()
            .catch { it.printStackTrace() }
            .onEach { //Observe advertiser lifecycle events
                if (it is OnAdvertisingSetStarted) { //Handle advertising start event
                    Log.d(TAG, "Advertising started.")
                }
                if (it is OnAdvertisingSetStopped) { //Handle advertising top event
                    Log.d(TAG, "Advertising stopped.")
                }
            }.launchIn(this)

        val server = ServerBleGatt.create(
            context = context,
            config = arrayOf(serviceConfig),
            scope = scope,
            mock = mockDevice
        )

        launch {
            while (true) {
                delay(1000)
                generateNewButtonValue()
            }
        }

        launch {
            server.connections
                .mapNotNull { it.values.firstOrNull() }
                .collect {
                    val service =
                        it.services.findService(BlinkySpecifications.UUID_SERVICE_DEVICE)!!
                    val ledCharacteristic =
                        service.findCharacteristic(BlinkySpecifications.UUID_LED_CHAR)!!
                    val buttonCharacteristic =
                        service.findCharacteristic(BlinkySpecifications.UUID_BUTTON_CHAR)!!

                    buttonState.onEach {
                        buttonCharacteristic.setValueAndNotifyClient(it)
                    }.launchIn(this)
                }
        }
    }

    private fun generateNewButtonValue(): DataByteArray {
        return if (buttonState.value == DataByteArray.from(0x00)) {
            DataByteArray.from(0x01)
        } else {
            DataByteArray.from(0x00)
        }.also {
            buttonState.value = it
        }
    }
}
