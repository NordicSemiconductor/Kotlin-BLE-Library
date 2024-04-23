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

package no.nordicsemi.android.kotlin.ble.server.api

import android.bluetooth.BluetoothGattServerCallback
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService

/**
 * An event class which maps [BluetoothGattServerCallback] callbacks into data classes.
 *
 * @see [BluetoothGattServerCallback](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback)
 */
sealed interface ServerGattEvent {

    /**
     * Indicates whether a local service has been added successfully.
     *
     * @property service The service that has been added.
     * @property status Status of the operation.
     *
     * @see [BluetoothGattServerCallback.onServiceAdded](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onServiceAdded(int,%20android.bluetooth.BluetoothGattService))
     */
    data class ServiceAdded(
        val service: IBluetoothGattService,
        val status: BleGattOperationStatus
    ) : ServerGattEvent

    /**
     * Interface grouping events related to a specific client device.
     */
    sealed interface GattClientConnectionEvent : ServerGattEvent {

        /**
         * A client device which has requested some action from the server.
         */
        val device: ClientDevice
    }

    /**
     * Event indicating when a remote device has been connected or disconnected.
     *
     * @property device Remote device that has been connected or disconnected.
     * @property status Status of the operation.
     * @property newState Returns the new connection state.
     *
     * @see [BluetoothGattServerCallback.onConnectionStateChange](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onConnectionStateChange(android.bluetooth.BluetoothDevice,%20int,%20int))
     */
    data class ClientConnectionStateChanged(
        override val device: ClientDevice,
        val status: BleGattConnectionStatus,
        val newState: GattConnectionState
    ) : GattClientConnectionEvent

    /**
     * Event triggered as result of [GattServerAPI.readPhy].
     *
     * @property device The remote device that requested the PHY read.
     * @property txPhy The transmitter PHY in use.
     * @property rxPhy The receiver PHY in use.
     * @property status Status of the operation.
     *
     * @see [BluetoothGattServerCallback.onPhyRead](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onPhyRead(android.bluetooth.BluetoothDevice,%20int,%20int,%20int))
     */
    data class ServerPhyRead(
        override val device: ClientDevice,
        val txPhy: BleGattPhy,
        val rxPhy: BleGattPhy,
        val status: BleGattOperationStatus
    ) : GattClientConnectionEvent

    /**
     * Event triggered as result of BluetoothGattServer#setPreferredPhy, or as a result of remote device
     * changing the PHY.
     *
     * @property device The remote device.
     * @property txPhy The transmitter PHY in use.
     * @property rxPhy The receiver PHY in use.
     * @property status Status of the operation.
     *
     * @see [BluetoothGattServerCallback.onPhyUpdate](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onPhyUpdate(android.bluetooth.BluetoothDevice,%20int,%20int,%20int))
     */
    data class ServerPhyUpdate(
        override val device: ClientDevice,
        val txPhy: BleGattPhy,
        val rxPhy: BleGattPhy,
        val status: BleGattOperationStatus
    ) : GattClientConnectionEvent

    /**
     * Event indicating the MTU for a given device connection has changed.
     * This callback will be invoked if a remote client has requested to change the MTU for
     * a given connection.
     *
     * @property device The remote device that requested the MTU change.
     * @property mtu The new MTU size.
     *
     * @see [BluetoothGattServerCallback.onMtuChanged](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onMtuChanged(android.bluetooth.BluetoothDevice,%20int))
     */
    data class ServerMtuChanged(
        override val device: ClientDevice,
        val mtu: Int
    ) : GattClientConnectionEvent

    /**
     * Interface grouping events related to services.
     */
    sealed interface ServiceEvent : GattClientConnectionEvent

    /**
     * Interface grouping events related to characteristics.
     */
    sealed interface CharacteristicEvent : ServiceEvent

    /**
     * A remote client has requested to read a local characteristic.
     *
     * An application must call [GattServerAPI.sendResponse] to complete the request.
     *
     * @property device The remote device that has requested the read operation
     * @property requestId The Id of the request.
     * @property offset Offset into the value of the characteristic.
     * @property characteristic Characteristic to be read.
     *
     * @see BluetoothGattServerCallback.onCharacteristicReadRequest[](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onCharacteristicReadRequest(android.bluetooth.BluetoothDevice,%20int,%20int,%20android.bluetooth.BluetoothGattCharacteristic))
     */
    data class CharacteristicReadRequest(
        override val device: ClientDevice,
        val requestId: Int,
        val offset: Int,
        val characteristic: IBluetoothGattCharacteristic
    ) : CharacteristicEvent

    /**
     * A remote client has requested to write to a local characteristic.
     *
     * An application must call [GattServerAPI.sendResponse] to complete the request.
     *
     * @property device The remote device that has requested the write operation.
     * @property requestId The Id of the request.
     * @property characteristic Characteristic to be written to.
     * @property preparedWrite True, if this write operation should be queued for later execution.
     * @property responseNeeded True, if the remote device requires a response.
     * @property offset The offset given for the value.
     * @property value The value the client wants to assign to the characteristic.
     *
     * @see [BluetoothGattServerCallback.onCharacteristicWriteRequest](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onCharacteristicWriteRequest(android.bluetooth.BluetoothDevice,%20int,%20android.bluetooth.BluetoothGattCharacteristic,%20boolean,%20boolean,%20int,%20byte[]))
     */
    data class CharacteristicWriteRequest(
        override val device: ClientDevice,
        val requestId: Int,
        val characteristic: IBluetoothGattCharacteristic,
        val preparedWrite: Boolean,
        val responseNeeded: Boolean,
        val offset: Int,
        val value: DataByteArray
    ) : CharacteristicEvent

    /**
     * Event emitted when a notification or indication has been sent to a remote device.
     *
     * When multiple notifications are to be sent, an application must wait for this callback to be
     * received before sending additional notifications.
     *
     * @property device The remote device the notification has been sent to.
     * @property status Status of the operation.
     *
     * @see [BluetoothGattServerCallback.onNotificationSent](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onNotificationSent(android.bluetooth.BluetoothDevice,%20int))
     */
    data class NotificationSent(
        override val device: ClientDevice,
        val status: BleGattOperationStatus
    ) : CharacteristicEvent

    /**
     * Interface grouping events related to descriptors.
     */
    sealed interface DescriptorEvent : ServiceEvent {
        val descriptor: IBluetoothGattDescriptor
    }

    /**
     * A remote client has requested to read a local descriptor.
     *
     * An application must call [GattServerAPI.sendResponse] to complete the request.
     *
     * @property device The remote device that has requested the read operation.
     * @property requestId The Id of the request.
     * @property offset Offset into the value of the characteristic.
     * @property descriptor Descriptor to be read.
     *
     * @see [BluetoothGattServerCallback.onDescriptorReadRequest](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onDescriptorReadRequest(android.bluetooth.BluetoothDevice,%20int,%20int,%20android.bluetooth.BluetoothGattDescriptor))
     */
    data class DescriptorReadRequest(
        override val device: ClientDevice,
        val requestId: Int,
        val offset: Int,
        override val descriptor: IBluetoothGattDescriptor
    ) : DescriptorEvent

    /**
     * A remote client has requested to write to a local descriptor.
     *
     * An application must call [GattServerAPI.sendResponse] to complete the request.
     *
     * @property device The remote device that has requested the write operation.
     * @property requestId The Id of the request.
     * @property descriptor Descriptor to be written to.
     * @property preparedWrite True, if this write operation should be queued for later execution.
     * @property responseNeeded True, if the remote device requires a response
     * @property offset The offset given for the value.
     * @property value The value the client wants to assign to the descriptor.
     *
     * @see [BluetoothGattServerCallback.onDescriptorWriteRequest](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onDescriptorWriteRequest(android.bluetooth.BluetoothDevice,%20int,%20android.bluetooth.BluetoothGattDescriptor,%20boolean,%20boolean,%20int,%20byte[]))
     */
    data class DescriptorWriteRequest(
        override val device: ClientDevice,
        val requestId: Int,
        override val descriptor: IBluetoothGattDescriptor,
        val preparedWrite: Boolean,
        val responseNeeded: Boolean,
        val offset: Int,
        val value: DataByteArray
    ) : DescriptorEvent

    /**
     * Execute all pending write operations for this device.
     *
     * An application must call [GattServerAPI.sendResponse] to complete the request.
     *
     * @property device The remote device that has requested the write operations.
     * @property requestId The Id of the request.
     * @property execute Whether the pending writes should be executed (true) or cancelled (false).
     *
     * @see [BluetoothGattServerCallback.onExecuteWrite](https://developer.android.com/reference/android/bluetooth/BluetoothGattServerCallback#onExecuteWrite(android.bluetooth.BluetoothDevice,%20int,%20boolean))
     */
    data class ExecuteWrite(
        override val device: ClientDevice,
        val requestId: Int,
        val execute: Boolean
    ) : ServiceEvent

}
