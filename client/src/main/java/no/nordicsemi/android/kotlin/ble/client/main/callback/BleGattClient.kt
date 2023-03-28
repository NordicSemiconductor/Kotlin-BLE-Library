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

package no.nordicsemi.android.kotlin.ble.client.main.callback

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattService
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.client.api.BleGatt
import no.nordicsemi.android.kotlin.ble.client.api.DataChangedEvent
import no.nordicsemi.android.kotlin.ble.client.api.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnPhyRead
import no.nordicsemi.android.kotlin.ble.client.api.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.client.api.OnReadRemoteRssi
import no.nordicsemi.android.kotlin.ble.client.api.OnServiceChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.client.main.ClientScope
import no.nordicsemi.android.kotlin.ble.client.main.service.BleGattServices
import no.nordicsemi.android.kotlin.ble.client.main.errors.DeviceDisconnectedException
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.PhyInfo
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BleGattClient(
    private val gatt: BleGatt
) {

    private val _connectionStateWithStatus = MutableStateFlow<Pair<GattConnectionState, BleGattConnectionStatus>?>(null)
    val connectionStateWithStatus = _connectionStateWithStatus.asStateFlow()

    val connectionState = _connectionStateWithStatus.mapNotNull { it?.first }

    private val _services = MutableStateFlow<BleGattServices?>(null)
    val services = _services.asStateFlow()

    private var onConnectionStateChangedCallback: ((GattConnectionState, BleGattConnectionStatus) -> Unit)? = null
    private var mtuCallback: ((Int) -> Unit)? = null
    private var rssiCallback: ((Int) -> Unit)? = null
    private var phyCallback: ((PhyInfo) -> Unit)? = null

    init {
        gatt.event.onEach {
            Log.d("AAATESTAAA", "Client event: $it")
            when (it) {
                is OnConnectionStateChanged -> onConnectionStateChange(it.status, it.newState)
                is OnServicesDiscovered -> onServicesDiscovered(it.services, it.status)
                is DataChangedEvent -> _services.value?.apply { onCharacteristicEvent(it) }
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
            if (connectionState == GattConnectionState.STATE_CONNECTED) {
                continuation.resume(Unit)
            } else if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
                continuation.resumeWithException(DeviceDisconnectedException(status))
            }
            onConnectionStateChangedCallback = null
        }
    }

    suspend fun requestMtu(mtu: Int): Int = suspendCoroutine { continuation ->
        mtuCallback = {
            continuation.resume(it)
            mtuCallback = null
        }
        gatt.requestMtu(mtu)
    }

    suspend fun readRssi(): Int = suspendCoroutine { continuation ->
        rssiCallback = {
            continuation.resume(it)
            rssiCallback = null
        }
        gatt.readRemoteRssi()
    }

    suspend fun setPhy(txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption): PhyInfo = suspendCoroutine { continuation ->
        phyCallback = {
            continuation.resume(it)
            phyCallback = null
        }
        gatt.setPreferredPhy(txPhy, rxPhy, phyOption)
    }

    fun disconnect() {
        gatt.disconnect()
    }

    fun clearServicesCache() {
        gatt.clearServicesCache()
    }

    @SuppressLint("MissingPermission")
    private fun onConnectionStateChange(status: BleGattConnectionStatus, connectionState: GattConnectionState) {
        _connectionStateWithStatus.value = connectionState to status
        onConnectionStateChangedCallback?.invoke(connectionState, status)

        if (status != BleGattConnectionStatus.SUCCESS) {
            gatt.close()
        } else if (connectionState == GattConnectionState.STATE_CONNECTED) {
            gatt.discoverServices()
        } else if (connectionState == GattConnectionState.STATE_DISCONNECTED) {
            if (!status.isLinkLoss || !gatt.autoConnect) {
                gatt.close()
            }
        }
    }

    private fun onServicesDiscovered(gattServices: List<BluetoothGattService>?, status: BleGattOperationStatus) {
        val services = gattServices?.let { BleGattServices(gatt, it) }
        _services.value = services
    }

    private fun onEvent(event: OnMtuChanged) {
        mtuCallback?.invoke(event.mtu)
    }

    private fun onEvent(event: OnPhyRead) {
        phyCallback?.invoke(PhyInfo(event.txPhy, event.rxPhy))
    }

    private fun onEvent(event: OnPhyUpdate) {
        phyCallback?.invoke(PhyInfo(event.txPhy, event.rxPhy))
    }

    private fun onEvent(event: OnReadRemoteRssi) {
        rssiCallback?.invoke(event.rssi)
    }

    @SuppressLint("MissingPermission")
    private fun onEvent(event: OnServiceChanged) {
        gatt.discoverServices()
    }
}
