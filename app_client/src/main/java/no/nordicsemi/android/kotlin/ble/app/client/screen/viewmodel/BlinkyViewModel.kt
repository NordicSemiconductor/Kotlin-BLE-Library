package no.nordicsemi.android.kotlin.ble.app.client.screen.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.app.client.BlinkyDestinationId
import no.nordicsemi.android.kotlin.ble.app.client.screen.repository.BlinkyButtonParser
import no.nordicsemi.android.kotlin.ble.app.client.screen.repository.BlinkyLedParser
import no.nordicsemi.android.kotlin.ble.app.client.screen.view.BlinkyViewState
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import java.util.*
import javax.inject.Inject

@SuppressLint("MissingPermission")
@HiltViewModel
class BlinkyViewModel @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val navigator: Navigator,
    private val savedStateHandle: SavedStateHandle
) : SimpleNavigationViewModel(navigator, savedStateHandle) {

    private val _device = MutableStateFlow<ServerDevice?>(null)
    val device = _device.asStateFlow()

    private val _state = MutableStateFlow(BlinkyViewState())
    val state = _state.asStateFlow()

    private var client: ClientBleGatt? = null

    init {
        val blinkyDevice = parameterOf(BlinkyDestinationId)
        _device.value = blinkyDevice
        startGattClient(blinkyDevice)
    }

    private lateinit var ledCharacteristic: ClientBleGattCharacteristic
    private lateinit var buttonCharacteristic: ClientBleGattCharacteristic

    private fun startGattClient(blinkyDevice: ServerDevice) = viewModelScope.launch {
        //Connect a Bluetooth LE device.
        val client = ClientBleGatt.connect(context, blinkyDevice, viewModelScope).also {
            this@BlinkyViewModel.client = it
        }

        if (!client.isConnected) {
            return@launch
        }

        //Discover services on the Bluetooth LE Device.
        val services = client.discoverServices()
        configureGatt(services)
    }

    private suspend fun configureGatt(services: ClientBleGattServices) {
        //Remember needed service and characteristics which are used to communicate with the DK.
        val service = services.findService(BlinkySpecifications.UUID_SERVICE_DEVICE)!!
        ledCharacteristic = service.findCharacteristic(BlinkySpecifications.UUID_LED_CHAR)!!
        buttonCharacteristic = service.findCharacteristic(BlinkySpecifications.UUID_BUTTON_CHAR)!!

        //Observe button characteristic which detects when a button is pressed
        buttonCharacteristic.getNotifications().onEach {
            _state.value = _state.value.copy(isButtonPressed = BlinkyButtonParser.isButtonPressed(it))
        }.launchIn(viewModelScope)

        //Check the initial state of the Led.
        val isLedOn = BlinkyLedParser.isLedOn(ledCharacteristic.read())
        _state.value = _state.value.copy(isLedOn = isLedOn)
    }

    @SuppressLint("NewApi")
    fun turnLed() {
        viewModelScope.launch {
            if (state.value.isLedOn) {
                _state.value = _state.value.copy(isLedOn = false)
                ledCharacteristic.write(DataByteArray.from(0x00))
            } else {
                _state.value = _state.value.copy(isLedOn = true)
                ledCharacteristic.write(DataByteArray.from(0x01))
            }
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            client?.disconnect()
            navigator.navigateUp()
        }
    }
}
