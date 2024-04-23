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

package no.nordicsemi.android.kotlin.ble.server.real

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.RealClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattService
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.CharacteristicReadRequest
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.CharacteristicWriteRequest
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.ClientConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.DescriptorReadRequest
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.DescriptorWriteRequest
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.ExecuteWrite
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.NotificationSent
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.ServerMtuChanged
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.ServerPhyRead
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.ServerPhyUpdate
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.ServiceAdded

/**
 * A class which maps [BluetoothGattServerCallback] methods into [ServerGattEvent] events.
 *
 * @param bufferSize A buffer size for events emitted by [BluetoothGattServerCallback].
 */
class ServerBleGattCallback(
    bufferSize: Int
) : BluetoothGattServerCallback() {

    private val _event = MutableSharedFlow<ServerGattEvent>(
        extraBufferCapacity = bufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event = _event.asSharedFlow()

    var onServiceAdded: ((IBluetoothGattService, BleGattOperationStatus) -> Unit)? = null

    override fun onCharacteristicReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        val native = NativeBluetoothGattCharacteristic(characteristic)
        _event.tryEmit(CharacteristicReadRequest(RealClientDevice(device!!), requestId, offset, native))
    }

    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        val native = NativeBluetoothGattCharacteristic(characteristic)
        _event.tryEmit(
            CharacteristicWriteRequest(
                RealClientDevice(device!!),
                requestId,
                native,
                preparedWrite,
                responseNeeded,
                offset,
                DataByteArray(value!!)
            )
        )
    }

    override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
        val operationStatus = BleGattConnectionStatus.create(status)
        val state = GattConnectionState.create(newState)
        _event.tryEmit(ClientConnectionStateChanged(RealClientDevice(device!!), operationStatus, state))
    }

    override fun onDescriptorReadRequest(
        device: BluetoothDevice?,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor
    ) {
        val native = NativeBluetoothGattDescriptor(descriptor)
        _event.tryEmit(DescriptorReadRequest(RealClientDevice(device!!), requestId, offset, native))
    }

    override fun onDescriptorWriteRequest(
        device: BluetoothDevice?,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray?
    ) {
        val native = NativeBluetoothGattDescriptor(descriptor)
        _event.tryEmit(
            DescriptorWriteRequest(
                RealClientDevice(device!!),
                requestId,
                native,
                preparedWrite,
                responseNeeded,
                offset,
                DataByteArray(value!!)
            )
        )
    }

    override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
        _event.tryEmit(ExecuteWrite(RealClientDevice(device!!), requestId, execute))
    }

    override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
        _event.tryEmit(ServerMtuChanged(RealClientDevice(device!!), mtu))
    }

    override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
        _event.tryEmit(NotificationSent(RealClientDevice(device!!), BleGattOperationStatus.create(status)))
    }

    override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        _event.tryEmit(
            ServerPhyRead(
                RealClientDevice(device!!),
                BleGattPhy.create(txPhy),
                BleGattPhy.create(rxPhy),
                BleGattOperationStatus.create(status)
            )
        )
    }

    override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
        _event.tryEmit(
            ServerPhyUpdate(
                RealClientDevice(device!!),
                BleGattPhy.create(txPhy),
                BleGattPhy.create(rxPhy),
                BleGattOperationStatus.create(status)
            )
        )
    }

    override fun onServiceAdded(status: Int, service: BluetoothGattService) {
        val native = NativeBluetoothGattService(service)
        val opStatus = BleGattOperationStatus.create(status)
        _event.tryEmit(ServiceAdded(native, opStatus))
        onServiceAdded?.invoke(native, opStatus)
    }

    fun onEvent(event: ServerGattEvent) {
        _event.tryEmit(event)
    }
}
