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

package no.nordicsemi.android.kotlin.ble.client.real

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.RestrictTo
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.*
import no.nordicsemi.android.kotlin.ble.client.api.ClientMutexHandleCallback
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattService

/**
 * A class which maps [BluetoothGattCallback] methods into [ClientGattEvent] events.
 *
 * @param bufferSize A buffer size for events emitted by [BluetoothGattCallback].
 */
class ClientBleGattCallback(
    bufferSize: Int,
    private val mutexWrapper: MutexWrapper
): BluetoothGattCallback() {

    private val _event = ClientMutexHandleCallback(bufferSize, mutexWrapper)
    val event = _event.event

    /**
     * Callback responsible for emitting an event [ServicesDiscovered].
     */
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        val services = gatt.services.map { NativeBluetoothGattService(it) }
        _event.tryEmit(ServicesDiscovered(services, BleGattOperationStatus.create(status)))
    }

    /**
     * Callback responsible for emitting an event [ConnectionStateChanged].
     */
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        _event.tryEmit(ConnectionStateChanged(BleGattConnectionStatus.create(status), GattConnectionState.create(newState)))
    }

    /**
     * Callback responsible for emitting an event [CharacteristicChanged].
     */
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {
        val native = NativeBluetoothGattCharacteristic(characteristic)
        _event.tryEmit(CharacteristicChanged(native, DataByteArray(value)))
    }

    /**
     * Callback responsible for emitting an event [CharacteristicChanged].
     */
    @Deprecated("In use for Android < 13")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?
    ) {
        characteristic?.let {
            val native = NativeBluetoothGattCharacteristic(it)
            _event.tryEmit(CharacteristicChanged(native, native.value))
        }
    }

    /**
     * Callback responsible for emitting an event [CharacteristicRead].
     */
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        val native = NativeBluetoothGattCharacteristic(characteristic)
        _event.tryEmit(CharacteristicRead(native, DataByteArray(value), BleGattOperationStatus.create(status)))
    }

    /**
     * Callback responsible for emitting an event [CharacteristicRead].
     */
    @Deprecated("In use for Android < 13")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let {
            val native = NativeBluetoothGattCharacteristic(characteristic)
            _event.tryEmit(CharacteristicRead(native, native.value, BleGattOperationStatus.create(status)))
        }
    }

    /**
     * Callback responsible for emitting an event [CharacteristicWrite].
     */
    override fun onCharacteristicWrite(
        gatt: BluetoothGatt?,
        characteristic: BluetoothGattCharacteristic?,
        status: Int
    ) {
        characteristic?.let {
            val native = NativeBluetoothGattCharacteristic(characteristic)
            _event.tryEmit(CharacteristicWrite(native, BleGattOperationStatus.create(status)))
        }
    }

    /**
     * Callback responsible for emitting an event [DescriptorRead].
     */
    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
        value: ByteArray
    ) {
        val native = NativeBluetoothGattDescriptor(descriptor)
        _event.tryEmit(DescriptorRead(native, DataByteArray(value), BleGattOperationStatus.create(status)))
    }

    /**
     * Callback responsible for emitting an event [DescriptorRead].
     */
    @Deprecated("In use for Android < 13")
    override fun onDescriptorRead(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let {
            val native = NativeBluetoothGattDescriptor(descriptor)
            _event.tryEmit(DescriptorRead(native, native.value, BleGattOperationStatus.create(status)))
        }
    }

    /**
     * Callback responsible for emitting an event [DescriptorWrite].
     */
    override fun onDescriptorWrite(
        gatt: BluetoothGatt?,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        descriptor?.let {
            val native = NativeBluetoothGattDescriptor(descriptor)
            _event.tryEmit(DescriptorWrite(native, BleGattOperationStatus.create(status)))
        }
    }

    /**
     * Callback responsible for emitting an event [MtuChanged].
     */
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
        _event.tryEmit(MtuChanged(mtu, BleGattOperationStatus.create(status)))
    }

    /**
     * Callback responsible for emitting an event [PhyRead].
     */
    override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        _event.tryEmit(
            PhyRead(
                BleGattPhy.create(txPhy),
                BleGattPhy.create(rxPhy),
                BleGattOperationStatus.create(status)
            )
        )
    }

    /**
     * Callback responsible for emitting an event [PhyUpdate].
     */
    override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
        _event.tryEmit(
            PhyUpdate(
                BleGattPhy.create(txPhy),
                BleGattPhy.create(rxPhy),
                BleGattOperationStatus.create(status)
            )
        )
    }

    /**
     * Callback responsible for emitting an event [ReadRemoteRssi].
     */
    override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
        _event.tryEmit(ReadRemoteRssi(rssi, BleGattOperationStatus.create(status)))
    }

    /**
     * Callback responsible for emitting an event [ReliableWriteCompleted].
     */
    override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
        _event.tryEmit(ReliableWriteCompleted(BleGattOperationStatus.create(status)))
    }

    /**
     * Callback responsible for emitting an event [ServiceChanged].
     */
    override fun onServiceChanged(gatt: BluetoothGatt) {
        _event.tryEmit(ServiceChanged())
    }

    /**
     * Auxiliary class for propagating events. For internal use only.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun onEvent(event: ClientGattEvent) {
        _event.tryEmit(event)
    }
}
