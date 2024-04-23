/*
 * Copyright (c) 2023, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.server.mock

import android.bluetooth.BluetoothGattServerCallback
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent

/**
 * A class for communication with [MockEngine]. It allows for sending responses to connected client
 * devices.
 *
 * @property mockEngine An instance of a [MockEngine].
 * @property serverDevice A server device.
 * @param bufferSize A buffer size for events.
 */
class MockServerAPI(
    private val mockEngine: MockEngine,
    private val serverDevice: MockServerDevice,
    bufferSize: Int,
) : GattServerAPI {

    //todo verify reply side-effects
    private val _event = MutableSharedFlow<ServerGattEvent>(replay = bufferSize, extraBufferCapacity = bufferSize, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override val event: SharedFlow<ServerGattEvent> = _event.asSharedFlow()

    override fun onEvent(event: ServerGattEvent) {
        _event.tryEmit(event)
    }

    override fun sendResponse(device: ClientDevice, requestId: Int, status: Int, offset: Int, value: DataByteArray?) {
        mockEngine.sendResponse(device, requestId, status, offset, value)
    }

    override fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: IBluetoothGattCharacteristic,
        confirm: Boolean,
        value: DataByteArray
    ) {
        mockEngine.notifyCharacteristicChanged(device, characteristic, confirm, value)
    }

    override fun close() {
        mockEngine.unregisterServer(serverDevice)
    }

    override fun cancelConnection(device: ClientDevice) {
        mockEngine.cancelConnection(serverDevice, device)
    }

    override fun connect(device: ClientDevice, autoConnect: Boolean) {
        mockEngine.connect(device)
    }

    override fun readPhy(device: ClientDevice) {
        mockEngine.readPhy(device)
    }

    override fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        mockEngine.requestPhy(device, txPhy, rxPhy, phyOption)
    }
}
