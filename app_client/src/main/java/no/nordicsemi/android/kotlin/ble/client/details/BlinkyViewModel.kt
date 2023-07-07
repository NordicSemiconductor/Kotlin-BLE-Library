/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.kotlin.ble.client.details

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
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.client.main.callback.BleGattClient
import no.nordicsemi.android.kotlin.ble.client.main.connect
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import java.util.*
import javax.inject.Inject

object BlinkySpecifications {
    /** Nordic Blinky Service UUID. */
    val UUID_SERVICE_DEVICE: UUID = UUID.fromString("00001523-1212-efde-1523-785feabcd123")

    /** LED characteristic UUID. */
    val UUID_LED_CHAR: UUID by lazy { UUID.fromString("00001525-1212-efde-1523-785feabcd123") }

    /** BUTTON characteristic UUID. */
    val UUID_BUTTON_CHAR: UUID by lazy { UUID.fromString("00001524-1212-efde-1523-785feabcd123") }
}

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

    private val _state = MutableStateFlow(BlinkyState())
    val state = _state.asStateFlow()

    private lateinit var ledCharacteristic: BleGattCharacteristic
    private lateinit var buttonCharacteristic: BleGattCharacteristic
    private lateinit var client: BleGattClient

    init {
        val blinkyDevice = parameterOf(BlinkyDestinationId)
        _device.value = blinkyDevice
        startGattClient(blinkyDevice)
    }

    private fun startGattClient(blinkyDevice: ServerDevice) = viewModelScope.launch {
        //Connect a Bluetooth LE device.
        client = blinkyDevice.connect(context)

        if (!client.isConnected) {
            return@launch
        }

        //Discover services on the Bluetooth LE Device.
        val services = client.discoverServices()
        configureGatt(services)
    }

    private suspend fun configureGatt(services: BleGattServices) {
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
                //Write is a suspend function which waits for the operation to finish.
                ledCharacteristic.write(byteArrayOf(0x00))
                //No exception means that writing was a success. We can update the UI.
                _state.value = _state.value.copy(isLedOn = false)
            } else {
                //Write is a suspend function which waits for the operation to finish.
                ledCharacteristic.write(byteArrayOf(0x01))
                //No exception means that writing was a success. We can update the UI.
                _state.value = _state.value.copy(isLedOn = true)
            }
        }
    }
}
