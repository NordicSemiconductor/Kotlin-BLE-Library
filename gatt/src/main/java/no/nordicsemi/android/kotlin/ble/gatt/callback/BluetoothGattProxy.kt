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

package no.nordicsemi.android.kotlin.ble.gatt.callback

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.gatt.event.GattEvent
import no.nordicsemi.android.kotlin.ble.gatt.event.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.gatt.event.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.gatt.event.OnCharacteristicWrite
import no.nordicsemi.android.kotlin.ble.gatt.event.OnConnectionStateChanged
import no.nordicsemi.android.kotlin.ble.gatt.event.OnDescriptorRead
import no.nordicsemi.android.kotlin.ble.gatt.event.OnDescriptorWrite
import no.nordicsemi.android.kotlin.ble.gatt.event.OnServicesDiscovered
import no.nordicsemi.android.kotlin.ble.gatt.event.BleGattOperationStatus

internal class BluetoothGattProxy(
    private val onEvent: (GattEvent) -> Unit
) : BluetoothGattCallback() {

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        gatt?.let { onEvent(OnServicesDiscovered(it, BleGattOperationStatus.create(status))) }
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        gatt?.let { onEvent(OnConnectionStateChanged(it, status, newState)) }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        onEvent(OnCharacteristicChanged(characteristic, value))
    }

    @Deprecated("In use for Android < 13")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        characteristic?.let {
            onEvent(OnCharacteristicChanged(characteristic, characteristic.value))
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        onEvent(OnCharacteristicRead(characteristic, value, BleGattOperationStatus.create(status)))
    }

    @Deprecated("In use for Android < 13")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let {
            onEvent(OnCharacteristicRead(characteristic, characteristic.value, BleGattOperationStatus.create(status)))
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let { onEvent(OnCharacteristicWrite(it, BleGattOperationStatus.create(status))) }
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray
    ) {
        onEvent(OnDescriptorRead(descriptor, value, BleGattOperationStatus.create(status)))
    }

    @Deprecated("In use for Android < 13")
    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let {
            onEvent(OnDescriptorRead(descriptor, descriptor.value, BleGattOperationStatus.create(status)))
        }
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let { onEvent(OnDescriptorWrite(it, BleGattOperationStatus.create(status))) }
    }
}
