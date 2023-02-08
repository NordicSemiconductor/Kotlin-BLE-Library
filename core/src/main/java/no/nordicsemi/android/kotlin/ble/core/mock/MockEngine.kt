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

package no.nordicsemi.android.kotlin.ble.core.mock

import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.client.BleMockGatt
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.server.GattServerEvent
import no.nordicsemi.android.kotlin.ble.core.server.MockServerAPI
import no.nordicsemi.android.kotlin.ble.core.server.OnConnectionStateChanged

internal object MockEngine {
    private val registeredServers = mutableMapOf<MockServerDevice, MockServerAPI>()
    private val registeredServersCallbacks = mutableMapOf<MockServerDevice, MutableSharedFlow<GattServerEvent>>()

    private val registeredClients = mutableMapOf<MockClientDevice, BleMockGatt>()
    private val registeredConnection = mutableMapOf<MockServerDevice, MockClientDevice>()

    private val pendingClients = mutableMapOf<MockServerDevice, BleMockGatt>()

    private fun registerServer(device: MockServerDevice, server: MockServerAPI): Flow<GattServerEvent> {
        registeredServers[device] = server
        return MutableSharedFlow<GattServerEvent>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_LATEST).also {
            registeredServersCallbacks[device] = it
        }
    }

    fun connectToServer(device: MockServerDevice, client: BleMockGatt) {
        val server = registeredServers[device]!!
        pendingClients[device] = client
        val clientDevice = MockClientDevice()
        registeredClients[clientDevice] = client
        registeredConnection[device] = clientDevice
        server.onEvent(OnConnectionStateChanged(clientDevice, BleGattOperationStatus.GATT_SUCCESS, GattConnectionState.STATE_CONNECTED))
    }

    fun sendResponse(device: ClientDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {
//        val client = registeredClients[registeredServers[device]]


    }

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        TODO("Not yet implemented")
    }

    fun close() {
        TODO("Not yet implemented")
    }

    fun connect(device: ClientDevice, autoConnect: Boolean) {
        TODO("Not yet implemented")
    }

    fun readPhy(device: ClientDevice) {
        TODO("Not yet implemented")
    }

    fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        TODO("Not yet implemented")
    }
}
