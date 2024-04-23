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

import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic

/**
 * Interface around native Android API. For real BLE connections it uses [BluetoothGattServer], whereas
 * for mock device it utilizes [MockEngine].
 */
interface GattServerAPI {

    /**
     * Flow which emits BLE events. For real BLE connections it collects events from
     * [BluetoothGattServerCallback] under the hood, for mock device it gets events from [MockEngine].
     */
    val event: SharedFlow<ServerGattEvent>

    /**
     * Internal function for propagating events to [event] shared flow. For internal usage only.
     *
     * @param event Equivalent of a callback method from [BluetoothGattServerCallback].
     */
    fun onEvent(event: ServerGattEvent)

    /**
     * Send a response to a read or write request to a remote device.
     *
     * It should be send as a response for one of this events:
     * - [OnCharacteristicReadRequest]
     * - [OnCharacteristicWriteRequest]
     * - [OnDescriptorReadRequest]
     * - [OnDescriptorWriteRequest]
     *
     * @param device The remote device to send this response to.
     * @param requestId The ID of the request that was received with the callback.
     * @param status The status of the request to be sent to the remote devices.
     * @param offset Value offset for partial read/write response.
     * @param value The value of the attribute that was read/written (optional).
     *
     * @see [BluetoothGattServer.sendResponse](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#sendResponse(android.bluetooth.BluetoothDevice,%20int,%20int,%20int,%20byte[]))
     */
    fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: DataByteArray?
    )

    /**
     * Send a notification or indication that a local characteristic has been updated.
     *
     * @param device The remote device to receive the notification/indication. This value cannot be null.
     * @param characteristic The local characteristic that has been updated.
     * @param confirm True to request confirmation from the client (indication) or false to send a notification
     * @param value
     *
     * @see [BluetoothGattServer.notifyCharacteristicChanged](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#notifyCharacteristicChanged(android.bluetooth.BluetoothDevice,%20android.bluetooth.BluetoothGattCharacteristic,%20boolean,%20byte[]))
     */
    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: IBluetoothGattCharacteristic,
        confirm: Boolean,
        value: DataByteArray
    )

    /**
     * Close this GATT server instance.
     *
     * Application should call this method as early as possible after it is done with this GATT server.
     *
     * @see [BluetoothGattServer.close](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#close())
     */
    fun close()

    /**
     * Disconnects an established connection, or cancels a connection attempt currently in progress.
     *
     * @param device The remote device.
     *
     * @see [BluetoothGattServer.cancelConnection](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#cancelConnection(android.bluetooth.BluetoothDevice))
     */
    fun cancelConnection(device: ClientDevice)

    /**
     * Initiate a connection to a Bluetooth GATT capable device.
     *
     * The connection may not be established right away, but will be completed when the remote
     * device is available. A [OnClientConnectionStateChanged] event will
     * be triggered when the connection state changes as a result of this function.
     *
     * The [autoConnect] parameter determines whether to actively connect to the remote device, or
     * rather passively scan and finalize the connection when the remote device is in
     * range/available. Generally, the first ever connection to a device should be direct
     * (autoConnect set to false) and subsequent connections to known devices should be invoked
     * with the autoConnect parameter set to true.
     *
     * @param device The remote device.
     * @param autoConnect Whether to directly connect to the remote device (false) or to automatically connect as soon as the remote device becomes available (true).
     *
     * @see [BluetoothGattServer.connect](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#connect(android.bluetooth.BluetoothDevice,%20boolean))
     */
    fun connect(device: ClientDevice, autoConnect: Boolean)

    /**
     * Reads the current transmitter PHY and receiver PHY of the connection.
     *
     * @param device The remote device to send this response to.
     *
     * @see [BluetoothGattServer.readPhy](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#readPhy(android.bluetooth.BluetoothDevice))
     */
    fun readPhy(device: ClientDevice)

    /**
     * Set the preferred connection PHY for this app. Please note that this is just
     * a recommendation, whether the PHY change will happen depends on other applications
     * preferences, local and remote controller capabilities.
     * Controller can override these settings.
     *
     * @param device The remote device to send this response to.
     * @param txPhy Preferred transmitter PHY.
     * @param rxPhy Preferred receiver PHY.
     * @param phyOption Preferred coding to use when transmitting on the LE Coded PHY.
     *
     * @see [BluetoothGattServer.setPreferredPhy](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer#setPreferredPhy(android.bluetooth.BluetoothDevice,%20int,%20int,%20int))
     */
    fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption)
}
