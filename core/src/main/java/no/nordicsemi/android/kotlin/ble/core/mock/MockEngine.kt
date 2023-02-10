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
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.client.BleMockGatt
import no.nordicsemi.android.kotlin.ble.core.client.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.client.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.server.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.core.server.api.MockServerAPI

internal object MockEngine {
    //TODO allow for many devices
    private val _advertisedServers = MutableStateFlow(emptyList<MockServerDevice>())
    internal val advertisedServers = _advertisedServers.asStateFlow()

    private val registeredServers = mutableMapOf<MockServerDevice, MockServerAPI>()
    private val registeredClients = mutableMapOf<MockClientDevice, BleMockGatt>()

    fun registerServer(server: MockServerAPI) {
        val device = MockServerDevice()
        registeredServers[device] = server
        advertiseServer(device)
    }

    fun connectToServer(device: MockServerDevice, client: BleMockGatt) {
        val server = registeredServers[device]!!
        val clientDevice = MockClientDevice()
        registeredClients[clientDevice] = client
        server.onEvent(OnConnectionStateChanged(clientDevice, BleGattOperationStatus.GATT_SUCCESS, GattConnectionState.STATE_CONNECTED))
    }

    private fun advertiseServer(device: MockServerDevice) {
        _advertisedServers.value = _advertisedServers.value + device
    }

    //Server side

    fun sendResponse(device: ClientDevice, requestId: Int, status: Int, offset: Int, value: ByteArray?) {

    }

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        registeredClients[device]?.onEvent(OnCharacteristicChanged(characteristic, value))
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

    //Client side

    fun writeCharacteristic(
        device: MockServerDevice,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: BleWriteType
    ) {
        TODO("Not yet implemented")
    }

    fun readCharacteristic(device: MockServerDevice, characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    fun enableCharacteristicNotification(device: MockServerDevice, characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    fun disableCharacteristicNotification(device: MockServerDevice, characteristic: BluetoothGattCharacteristic) {
        TODO("Not yet implemented")
    }

    fun writeDescriptor(device: MockServerDevice, descriptor: BluetoothGattDescriptor, value: ByteArray) {
        TODO("Not yet implemented")
    }

    fun readDescriptor(device: MockServerDevice, descriptor: BluetoothGattDescriptor) {
        TODO("Not yet implemented")
    }

    fun readRemoteRssi(device: MockServerDevice) {
        TODO("Not yet implemented")
    }

    fun readPhy(device: MockServerDevice) {
        TODO("Not yet implemented")
    }

    fun discoverServices(device: MockServerDevice) {
        TODO("Not yet implemented")
    }

    fun setPreferredPhy(device: MockServerDevice, txPhy: Int, rxPhy: Int, phyOptions: Int) {
        TODO("Not yet implemented")
    }
}
