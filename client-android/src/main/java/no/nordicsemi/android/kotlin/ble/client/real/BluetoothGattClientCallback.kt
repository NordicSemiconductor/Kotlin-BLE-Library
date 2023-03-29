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

package no.nordicsemi.android.kotlin.ble.client.real

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.client.api.GattEvent
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnDescriptorRead
import no.nordicsemi.android.kotlin.ble.client.api.OnDescriptorWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnPhyRead
import no.nordicsemi.android.kotlin.ble.client.api.OnPhyUpdate
import no.nordicsemi.android.kotlin.ble.client.api.OnReadRemoteRssi
import no.nordicsemi.android.kotlin.ble.client.api.OnReliableWriteCompleted
import no.nordicsemi.android.kotlin.ble.client.api.OnServiceChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState

class BluetoothGattClientCallback: BluetoothGattCallback() {

    private val _event = MutableSharedFlow<GattEvent>(
        extraBufferCapacity = 10, //Warning: because of this parameter we can miss notifications
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val event = _event.asSharedFlow()


    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        _event.tryEmit(OnServicesDiscovered(gatt!!.services, BleGattOperationStatus.create(status)))
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        _event.tryEmit(OnConnectionStateChanged(BleGattConnectionStatus.create(status), GattConnectionState.create(newState)))
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        _event.tryEmit(OnCharacteristicChanged(characteristic, value))
    }

    @Deprecated("In use for Android < 13")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        characteristic?.let {
            _event.tryEmit(OnCharacteristicChanged(characteristic, characteristic.value))
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        _event.tryEmit(OnCharacteristicRead(characteristic, value, BleGattOperationStatus.create(status)))
    }

    @Deprecated("In use for Android < 13")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let {
            _event.tryEmit(OnCharacteristicRead(characteristic, characteristic.value, BleGattOperationStatus.create(status)))
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let {
            _event.tryEmit(OnCharacteristicWrite(it, BleGattOperationStatus.create(status)))
        }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray
    ) {
        _event.tryEmit(OnDescriptorRead(descriptor, value, BleGattOperationStatus.create(status)))
    }

    @Deprecated("In use for Android < 13")
    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let {
            _event.tryEmit(OnDescriptorRead(descriptor, descriptor.value, BleGattOperationStatus.create(status)))
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let { _event.tryEmit(OnDescriptorWrite(it, BleGattOperationStatus.create(status))) }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        _event.tryEmit(OnMtuChanged(mtu, BleGattOperationStatus.create(status)))
    }

    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        _event.tryEmit(
            OnPhyRead(
                BleGattPhy.create(txPhy),
                BleGattPhy.create(rxPhy),
                BleGattOperationStatus.create(status)
            )
        )
    }

    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        _event.tryEmit(
            OnPhyUpdate(
                BleGattPhy.create(txPhy),
                BleGattPhy.create(rxPhy),
                BleGattOperationStatus.create(status)
            )
        )
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        _event.tryEmit(OnReadRemoteRssi(rssi, BleGattOperationStatus.create(status)))
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        _event.tryEmit(OnReliableWriteCompleted(BleGattOperationStatus.create(status)))
    }

    override fun onServiceChanged(gatt: BluetoothGatt) {
        _event.tryEmit(OnServiceChanged())
    }

    fun onEvent(event: GattEvent) {
        _event.tryEmit(event)
    }
}
