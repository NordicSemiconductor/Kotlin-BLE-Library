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

package no.nordicsemi.android.kotlin.ble.client.callback

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import no.nordicsemi.android.kotlin.ble.client.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.client.errors.DeviceDisconnectedException
import no.nordicsemi.android.kotlin.ble.client.errors.ServicesNotDiscoveredException
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.client.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.event.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.client.event.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.client.service.BleGattServices
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BleGattConnection {

    private var gatt: BluetoothGatt? = null

    private val gattProxy: BluetoothGattClientCallback = BluetoothGattClientCallback {
        Log.d("AAATESTAAA", "Client event: $it")
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

    private var onServicesDiscoveredCallback: ((BleGattServices?, BleGattOperationStatus) -> Unit)? = null
    private var onConnectionStateChangedCallback: ((GattConnectionState, BleGattOperationStatus) -> Unit)? = null

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal suspend fun connect(
        context: Context,
        options: BleGattConnectOptions,
        device: BluetoothDevice
    ) = suspendCoroutine { continuation ->
        onConnectionStateChangedCallback = { connectionState, status ->
            if (connectionState == GattConnectionState.STATE_CONNECTED) {
                continuation.resume(Unit)
            } else if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
                continuation.resumeWithException(DeviceDisconnectedException(status))
            }
            onConnectionStateChangedCallback = null
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            device.connectGatt(context, options.autoConnect, gattProxy, BluetoothDevice.TRANSPORT_LE, options.getPhy())
        } else {
            device.connectGatt(context, options.autoConnect, gattProxy)
        }
    }

    private fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        val connectionState = GattConnectionState.create(newState)
        _connectionState.value = connectionState
        gatt?.let { this.gatt = it }
        onConnectionStateChangedCallback?.invoke(connectionState, BleGattOperationStatus.create(status))
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun getServices(): BleGattServices {
        if (_connectionState.value != GattConnectionState.STATE_CONNECTED) {
            throw IllegalStateException("Gatt should be connected before service discovery.")
        }

        services.firstOrNull()?.let { return it }

        return suspendCoroutine { continuation ->
            onServicesDiscoveredCallback = { services, status ->
                if (services != null) {
                    continuation.resume(services)
                } else {
                    continuation.resumeWithException(ServicesNotDiscoveredException(status))
                }
                onServicesDiscoveredCallback = null
            }
            gatt?.discoverServices()
        }
    }

    private fun onServicesDiscovered(gatt: BluetoothGatt?, status: BleGattOperationStatus) {
        val services = gatt?.services?.let { BleGattServices(gatt, it) }
        _services.value = services
        onServicesDiscoveredCallback?.invoke(services, status)
    }
}
