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

package no.nordicsemi.android.kotlin.ble.core.client.callback

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.core.BleDevice
import no.nordicsemi.android.kotlin.ble.core.client.BleClient
import no.nordicsemi.android.kotlin.ble.core.client.BleGatt
import no.nordicsemi.android.kotlin.ble.core.client.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.client.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.core.client.ClientScope
import no.nordicsemi.android.kotlin.ble.core.client.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.core.client.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.core.client.OnPhyRead
import no.nordicsemi.android.kotlin.ble.core.client.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.core.client.OnReadRemoteRssi
import no.nordicsemi.android.kotlin.ble.core.client.OnServiceChanged
import no.nordicsemi.android.kotlin.ble.core.client.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.core.client.errors.DeviceDisconnectedException
import no.nordicsemi.android.kotlin.ble.core.client.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BleGattClient(
    private val gatt: BleGatt
) : BleClient {

    private val _connection = MutableStateFlow(BleGattConnection())
    override val connection = _connection.asStateFlow()

    private var onConnectionStateChangedCallback: ((GattConnectionState, BleGattOperationStatus) -> Unit)? = null

    init {
        gatt.event.onEach {
            when (it) {
                is OnConnectionStateChanged -> onConnectionStateChange(it.status, it.newState)
                is OnServicesDiscovered -> onServicesDiscovered(it.gatt, it.status)
                is CharacteristicEvent -> _connection.value.services?.apply { onCharacteristicEvent(it) }
                is OnMtuChanged -> onEvent(it)
                is OnPhyRead -> onEvent(it)
                is OnPhyUpdate -> onEvent(it)
                is OnReadRemoteRssi -> onEvent(it)
                is OnServiceChanged -> onEvent(it)
            }
        }.launchIn(ClientScope)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    internal suspend fun connect() = suspendCoroutine { continuation ->
        onConnectionStateChangedCallback = { connectionState, status ->
            Log.d("AAATESTAAA", "State: $connectionState, Status: $status")
            if (connectionState == GattConnectionState.STATE_CONNECTED) {
                continuation.resume(Unit)
            } else if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
                continuation.resumeWithException(DeviceDisconnectedException(status))
            }
            onConnectionStateChangedCallback = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun onConnectionStateChange(status: BleGattOperationStatus, newState: Int) {
        val connectionState = GattConnectionState.create(newState)
        _connection.value = _connection.value.copy(connectionState = connectionState)
        onConnectionStateChangedCallback?.invoke(connectionState, status)

        if (connectionState == GattConnectionState.STATE_CONNECTED) {
            gatt.discoverServices()
        }
    }

    private fun onServicesDiscovered(g: BluetoothGatt?, status: BleGattOperationStatus) {
        val services = g?.services?.let { BleGattServices(gatt, it) }
        _connection.value = _connection.value.copy(services = services)
    }

    private fun onEvent(event: OnMtuChanged) {
        val params = _connection.value.connectionParams
        _connection.value = _connection.value.copy(connectionParams = params.copy(mtu = event.mtu))
    }

    private fun onEvent(event: OnPhyRead) {
        val params = _connection.value.connectionParams
        _connection.value =
            _connection.value.copy(connectionParams = params.copy(txPhy = event.txPhy, rxPhy = event.rxPhy))
    }

    private fun onEvent(event: OnPhyUpdate) {
        val params = _connection.value.connectionParams
        _connection.value =
            _connection.value.copy(connectionParams = params.copy(txPhy = event.txPhy, rxPhy = event.rxPhy))
    }

    private fun onEvent(event: OnReadRemoteRssi) {
        val params = _connection.value.connectionParams
        _connection.value = _connection.value.copy(connectionParams = params.copy(rssi = event.rssi))
    }

    @SuppressLint("MissingPermission")
    private fun onEvent(event: OnServiceChanged) {
        gatt.discoverServices()
    }
}
