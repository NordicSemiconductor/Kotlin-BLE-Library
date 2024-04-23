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

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import androidx.annotation.IntRange
import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectionPriority
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor

/**
 * Interface around native Android API. For real BLE connections it uses [BluetoothGatt], whereas
 * for mock device it utilizes [MockEngine].
 */
interface GattClientAPI {

    /**
     * Flow which emits BLE events. For real BLE connections it collects events from
     * [BluetoothGattCallback] under the hood, for mock device it gets events from [MockEngine].
     */
    val event: SharedFlow<ClientGattEvent>

    /**
     * A server device which has been used to establish connection. It can be either mock or real
     * BLE server device.
     */
    val device: ServerDevice

    /**
     * Parameter obtained from [BleGattConnectOptions.autoConnect] created during connection.
     */
    val autoConnect: Boolean

    /**
     * Parameter indicating that Gatt should be closed on first disconnected event.
     */
    val closeOnDisconnect: Boolean

    /**
     * Internal function for propagating events to [event] shared flow. For internal usage only.
     *
     * @param event equivalent of a callback method from [BluetoothGattCallback]
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun onEvent(event: ClientGattEvent)

    /**
     * Writes value to a characteristic.
     *
     * @param characteristic A characteristic to which the value will be written.
     * @param value A value as [DataByteArray].
     * @param writeType A write type method [BleWriteType].
     */
    fun writeCharacteristic(
        characteristic: IBluetoothGattCharacteristic,
        value: DataByteArray,
        writeType: BleWriteType
    ): Boolean

    /**
     * Reads value from a characteristic.
     *
     * @param characteristic A characteristic from which the value will be read.
     */
    fun readCharacteristic(characteristic: IBluetoothGattCharacteristic): Boolean

    /**
     * Enables notifications on a characteristic.
     *
     * @param characteristic A characteristic on which notifications will be enabled.
     */
    fun enableCharacteristicNotification(characteristic: IBluetoothGattCharacteristic): Boolean

    /**
     * Disables notifications on a characteristic.
     *
     * @param characteristic A characteristic on which notifications will be disabled.
     */
    fun disableCharacteristicNotification(characteristic: IBluetoothGattCharacteristic): Boolean

    /**
     * Writes value to a descriptor.
     *
     * @param descriptor A descriptor to which the value will be written.
     * @param value A value as [DataByteArray].
     */
    fun writeDescriptor(descriptor: IBluetoothGattDescriptor, value: DataByteArray): Boolean

    /**
     * Reads value from a descriptor.
     *
     * @param descriptor A descriptor from which the value will be read.
     */
    fun readDescriptor(descriptor: IBluetoothGattDescriptor): Boolean

    /**
     * Requests mtu. Max value is 517, min 23.
     *
     * @param mtu A mtu value.
     */
    fun requestMtu(@IntRange(from = 23, to = 517) mtu: Int): Boolean

    /**
     * Reads rssi of a remote server device.
     */
    fun readRemoteRssi(): Boolean

    /**
     * Reads phy properties of the connection
     */
    fun readPhy()

    /**
     * Discover services available on a remote server device.
     */
    fun discoverServices(): Boolean

    /**
     * Sets preferred phy for the connection.
     *
     * @param txPhy Phy ([BleGattPhy]) of a transmitter.
     * @param rxPhy Phy ([BleGattPhy]) of a receiver.
     * @param phyOption Phy option ([PhyOption]).
     */
    fun setPreferredPhy(txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption)

    /**
     * Disconnects from a peripheral.
     */
    fun disconnect()

    /**
     * Connects to a peripheral after disconnection. Works only if [BleGattConnectOptions.closeOnDisconnect] is set to false.
     */
    fun reconnect(): Boolean

    /**
     * Clears services cache. It should invoke [BluetoothGattCallback.onServiceChanged] callback.
     */
    fun clearServicesCache()

    /**
     * Releases connection resources. It should be called after [disconnect] succeeds.
     */
    fun close()

    /**
     * Begins reliable write. All writes to a characteristics which supports this feature will be
     * transactional which means that they can be reverted in case of data inconsistency.
     */
    fun beginReliableWrite(): Boolean

    /**
     * Aborts reliable write. All writes to a characteristics which supports reliable writes will be
     * reverted to a state preceding call to [beginReliableWrite].
     */
    fun abortReliableWrite()

    /**
     * Executes reliable write. All writes to a characteristics which supports reliable write will be
     * executed and new values will be set permanently.
     */
    fun executeReliableWrite(): Boolean

    /**
     * Requests connection priority. It will influence latency and power consumption.
     *
     * @param priority Requested [BleGattConnectionPriority].
     */
    fun requestConnectionPriority(priority: BleGattConnectionPriority): Boolean
}
