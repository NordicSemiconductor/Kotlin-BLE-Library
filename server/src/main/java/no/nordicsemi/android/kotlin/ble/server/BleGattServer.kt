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

package no.nordicsemi.android.kotlin.ble.server

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.server.callback.BleGattServerCallback
import no.nordicsemi.android.kotlin.ble.server.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.server.event.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.server.event.OnServiceAdded
import no.nordicsemi.android.kotlin.ble.server.service.BleGattServerService
import no.nordicsemi.android.kotlin.ble.server.service.BleGattServerServiceConfig
import no.nordicsemi.android.kotlin.ble.server.service.BleGattServerServices
import no.nordicsemi.android.kotlin.ble.server.service.BluetoothGattServiceFactory

class BleGattServer {

    private val connections = mutableMapOf<BluetoothDevice, BleGattServerServices>()

    private val callback = BleGattServerCallback { event ->
        when (event) {
            is OnConnectionStateChanged -> onConnectionStateChanged(event.device, event.status, event.newState)
            is OnServiceAdded -> onServiceAdded(event.service, event.status)
            is CharacteristicEvent -> connections.values.forEach { it.onEvent(event) }
        }
    }

    private var services: List<BleGattServerService> = emptyList()

    private var bluetoothGattServer: BluetoothGattServer? = null

    private fun onConnectionStateChanged(device: BluetoothDevice, status: Int, newState: Int) {
        val bleStatus = BleGattOperationStatus.create(status) //TODO consume status?
        val connectionState = GattConnectionState.create(newState)

        val copiedServices = services.map {
            BleGattServerService(bluetoothGattServer!!, BluetoothGattServiceFactory.copy(it.service))
        }

        when (connectionState) {
            GattConnectionState.STATE_CONNECTED -> connections[device] = BleGattServerServices(bluetoothGattServer!!, copiedServices)
            GattConnectionState.STATE_DISCONNECTED,
            GattConnectionState.STATE_CONNECTING,
            GattConnectionState.STATE_DISCONNECTING -> connections.remove(device)
        }
    }

    private fun onServiceAdded(service: BluetoothGattService, status: Int) {
        val serviceStatus = BleGattOperationStatus.create(status)
        bluetoothGattServer?.let { server ->
            if (serviceStatus == BleGattOperationStatus.GATT_SUCCESS) {
                services = services + BleGattServerService(server, service)
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun create(context: Context, config: List<BleGattServerServiceConfig>) {
        val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        val bluetoothGattServer = bluetoothManager.openGattServer(context, callback)

        config.forEach {
            bluetoothGattServer.addService(BluetoothGattServiceFactory.create(it))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun stopServer() {
        bluetoothGattServer?.close()
    }
}
