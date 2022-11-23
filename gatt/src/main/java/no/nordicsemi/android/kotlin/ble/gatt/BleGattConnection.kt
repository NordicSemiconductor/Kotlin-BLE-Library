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

package no.nordicsemi.android.kotlin.ble.gatt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.util.Log
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.kotlin.ble.gatt.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.gatt.event.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.gatt.event.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.gatt.service.BleGattServices
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleGattConnection {

    internal val gattProxy: BluetoothGattProxy = BluetoothGattProxy {
        when (it) {
            is OnConnectionStateChanged -> onConnectionStateChange(it.gatt, it.status, it.newState)
            is OnServicesDiscovered -> onServicesDiscovered(it.gatt, it.status)
            is CharacteristicEvent -> services.value?.apply { onCharacteristicEvent(it) }
        }
    }

    private val _connectionState = MutableStateFlow(GattConnectionState.STATE_DISCONNECTED)
    val connectionState = _connectionState.asStateFlow()

    private val _services = MutableStateFlow<BleGattServices?>(null)
    val services = _services.asStateFlow()

    private var onServicesDiscoveredCallback: (() -> Unit)? = null

    @SuppressLint("MissingPermission")
    private fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

        val connectionState = GattConnectionState.create(newState)
        _connectionState.value = connectionState

        if (connectionState == GattConnectionState.STATE_CONNECTED) {
            gatt?.discoverServices()
        } else if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
            _services.value = null
            gatt?.close()
        }
    }

    suspend fun getServices(): BleGattServices {
        return services.firstOrNull()
            ?: suspendCoroutine { continuation ->
                onServicesDiscoveredCallback = { continuation.resume(services.value!!) }
            }
    }

    private fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        _services.value = gatt?.services?.let { BleGattServices(gatt, it) }
        onServicesDiscoveredCallback?.invoke()
    }
}
