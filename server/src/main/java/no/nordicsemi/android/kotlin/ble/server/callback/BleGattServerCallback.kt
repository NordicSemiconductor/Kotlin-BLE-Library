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

package no.nordicsemi.android.kotlin.ble.server.callback

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.event.GattServerEvent
import no.nordicsemi.android.kotlin.ble.server.event.OnCharacteristicReadRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnCharacteristicWriteRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.server.event.OnDescriptorReadRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnDescriptorWriteRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnExecuteWrite
import no.nordicsemi.android.kotlin.ble.server.event.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.server.event.OnNotificationSent
import no.nordicsemi.android.kotlin.ble.server.event.OnPhyRead
import no.nordicsemi.android.kotlin.ble.server.event.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.server.event.OnServiceAdded

internal class BleGattServerCallback(
    private val onEvent: (GattServerEvent) -> Unit
) : BluetoothGattServerCallback() {

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic?
    ) {
        onEvent(OnCharacteristicReadRequest(device, requestId, offset, characteristic))
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        onEvent(OnCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value))
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        onEvent(OnConnectionStateChanged(device!!, status, newState))
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor?
    ) {
        onEvent(OnDescriptorReadRequest(device, requestId, offset, descriptor))
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor?,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        onEvent(OnDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value))
    }

    override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
        onEvent(OnExecuteWrite(device, requestId, execute))
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        onEvent(OnMtuChanged(device, mtu))
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        onEvent(OnNotificationSent(device, status))
    }

    override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        onEvent(OnPhyRead(device, txPhy, rxPhy, status))
    }

    override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        onEvent(OnPhyUpdate(device, txPhy, rxPhy, status))
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
        onEvent(OnServiceAdded(service!!, status))
    }
}
