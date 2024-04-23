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

package no.nordicsemi.android.kotlin.ble.client.api

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.BondState
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService

/**
 * An event class which maps [BluetoothGattCallback] callbacks into data classes.
 *
 * @see [BluetoothGattCallback](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback)
 */
sealed interface ClientGattEvent {

    /**
     * Event emitted when the list of remote services, characteristics and descriptors for the remote
     * device have been updated, i.e. new services have been discovered.
     *
     * @property services Discovered services.
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onServicesDiscovered](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onServicesDiscovered(android.bluetooth.BluetoothGatt,%20int))
     */
    data class ServicesDiscovered(
        val services: List<IBluetoothGattService>,
        val status: BleGattOperationStatus
    ) : ClientGattEvent

    /**
     * Event emitted when GATT client has connected/disconnected to/from a remote GATT server.
     *
     * @property status Operation status ([BleGattOperationStatus]).
     * @property newState New connection status ([BleGattConnectionStatus]).
     *
     * @see [BluetoothGattCallback.onConnectionStateChange](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt,%20int,%20int))
     */
    data class ConnectionStateChanged(
        val status: BleGattConnectionStatus,
        val newState: GattConnectionState
    ) : ClientGattEvent

    /**
     * Event emitted as result of [BluetoothGatt.readPhy].
     *
     * @property txPhy The transmitter PHY ([BleGattPhy]).
     * @property rxPhy The receiver PHY ([BleGattPhy]).
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onPhyRead](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onPhyRead(android.bluetooth.BluetoothGatt,%20int,%20int,%20int))
     */
    data class PhyRead(
        val txPhy: BleGattPhy,
        val rxPhy: BleGattPhy,
        val status: BleGattOperationStatus
    ) : ClientGattEvent

    /**
     * Event emitted as result of [BluetoothGatt.setPreferredPhy], or as a result of remote device
     * changing the PHY.
     *
     * @property txPhy The transmitter PHY ([BleGattPhy]).
     * @property rxPhy The receiver PHY ([BleGattPhy]).
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onPhyUpdate](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onPhyUpdate(android.bluetooth.BluetoothGatt,%20int,%20int,%20int))
     */
    data class PhyUpdate(
        val txPhy: BleGattPhy,
        val rxPhy: BleGattPhy,
        val status: BleGattOperationStatus
    ) : ClientGattEvent

    /**
     * Event reporting the RSSI for a remote device connection. This callback is triggered in
     * response to the [BluetoothGatt.readRemoteRssi] function.
     *
     * @property rssi The RSSI value for the remote device.
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onReadRemoteRssi](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onReadRemoteRssi(android.bluetooth.BluetoothGatt,%20int,%20int))
     */
    data class ReadRemoteRssi(val rssi: Int, val status: BleGattOperationStatus) : ClientGattEvent

    /**
     * This is an additional event which doesn't exist in [BluetoothGattCallback].
     * It is added here to make events complete, but the source of the emission comes from
     * [BondingBroadcastReceiver].
     *
     * @property bondState New bond state ([BondState]) of the remote device.
     *
     * @see [BluetoothDevice.ACTION_BOND_STATE_CHANGED]
     */
    data class BondStateChanged(val bondState: BondState) : ClientGattEvent

    /**
     * An event indicating "service changed" event is received.
     * Receiving this event means that the GATT database is out of sync with the remote device.
     * [BluetoothGatt.discoverServices] should be called to re-discover the services.
     *
     * @see [BluetoothGattCallback.onServiceChanged](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onServiceChanged(android.bluetooth.BluetoothGatt))
     */
    class ServiceChanged : ClientGattEvent

    /**
     * Interface which groups service related events.
     */
    sealed interface ServiceEvent : ClientGattEvent

    /**
     * Interface which groups characteristic related events.
     */
    sealed interface CharacteristicEvent : ServiceEvent

    /**
     * Interface which groups descriptor related event.
     */
    sealed interface DescriptorEvent : ServiceEvent

    /**
     * Event indicating the MTU for a given device connection has changed. This callback is triggered
     * in response to the [BluetoothGatt.requestMtu] function, or in response to a connection event.
     *
     * @property mtu The new MTU size.
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onMtuChanged](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onMtuChanged(android.bluetooth.BluetoothGatt,%20int,%20int))
     */
    data class MtuChanged(val mtu: Int, val status: BleGattOperationStatus) : ClientGattEvent

    /**
     * Event triggered as a result of a remote characteristic notification. Note that the value
     * within the characteristic object may have changed since receiving the remote characteristic
     * notification, so check the parameter value for the value at the time of notification.
     *
     * @property characteristic A characteristic that has been updated as a result of a remote notification event.
     * @property value Notified characteristic value.
     *
     * @see [BluetoothGattCallback.onCharacteristicChanged](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onCharacteristicChanged(android.bluetooth.BluetoothGatt,%20android.bluetooth.BluetoothGattCharacteristic,%20byte[]))
     */
    data class CharacteristicChanged(
        val characteristic: IBluetoothGattCharacteristic,
        val value: DataByteArray
    ) : CharacteristicEvent

    /**
     * Event reporting the result of a characteristic read operation.
     *
     * @property characteristic Characteristic that was read from the associated remote device.
     * @property value The value of the characteristic.
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onCharacteristicRead](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt,%20android.bluetooth.BluetoothGattCharacteristic,%20byte[],%20int))
     */
    data class CharacteristicRead(
        val characteristic: IBluetoothGattCharacteristic,
        val value: DataByteArray,
        val status: BleGattOperationStatus
    ) : CharacteristicEvent

    /**
     * Event indicating the result of a characteristic write operation.
     *
     * If this callback is invoked while a reliable write transaction is in progress, the value of
     * the characteristic represents the value reported by the remote device. An application should
     * compare this value to the desired value to be written. If the values don't match, the application
     * must abort the reliable write transaction.
     *
     * @property characteristic Characteristic that was written to the associated remote device.
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onCharacteristicWrite](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onCharacteristicWrite(android.bluetooth.BluetoothGatt,%20android.bluetooth.BluetoothGattCharacteristic,%20int))
     */
    data class CharacteristicWrite(
        val characteristic: IBluetoothGattCharacteristic,
        val status: BleGattOperationStatus
    ) : CharacteristicEvent

    /**
     * Event reporting the result of a descriptor read operation.
     *
     * @property descriptor Descriptor that was read from the associated remote device.
     * @property value The descriptor value at the time of the read operation
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onDescriptorRead](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onDescriptorRead(android.bluetooth.BluetoothGatt,%20android.bluetooth.BluetoothGattDescriptor,%20int,%20byte[]))
     */
    data class DescriptorRead(
        val descriptor: IBluetoothGattDescriptor,
        val value: DataByteArray,
        val status: BleGattOperationStatus
    ) : DescriptorEvent

    /**
     * Event indicating the result of a descriptor write operation.
     *
     * @property descriptor Descriptor that was writte to the associated remote device.
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onDescriptorWrite](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onDescriptorWrite(android.bluetooth.BluetoothGatt,%20android.bluetooth.BluetoothGattDescriptor,%20int))
     */
    data class DescriptorWrite(
        val descriptor: IBluetoothGattDescriptor,
        val status: BleGattOperationStatus
    ) : DescriptorEvent

    /**
     * Event invoked when a reliable write transaction has been completed.
     *
     * @property status Operation status ([BleGattOperationStatus]).
     *
     * @see [BluetoothGattCallback.onReliableWriteCompleted](https://developer.android.com/reference/android/bluetooth/BluetoothGattCallback#onReliableWriteCompleted(android.bluetooth.BluetoothGatt,%20int))
     */
    data class ReliableWriteCompleted(val status: BleGattOperationStatus) : ServiceEvent

}
