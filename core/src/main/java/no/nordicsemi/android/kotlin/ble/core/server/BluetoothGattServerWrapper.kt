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

package no.nordicsemi.android.kotlin.ble.core.server

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.os.Build
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.MockClientDevice
import no.nordicsemi.android.kotlin.ble.core.RealClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.mock.MockEngine
import no.nordicsemi.android.kotlin.ble.core.server.callback.BleGattServerCallback

@SuppressLint("MissingPermission")
internal class BluetoothGattServerWrapper(
    private val server: BluetoothGattServer,
    private val callback: BleGattServerCallback,
    private val mockEngine: MockEngine
) {

    val event: SharedFlow<GattServerEvent> = callback.event

    fun sendResponse(
        device: ClientDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray?
    ) {
        when (device) {
            is MockClientDevice -> mockEngine.sendResponse(device, requestId, status, offset, value)
            is RealClientDevice -> server.sendResponse(device.device, requestId, status, offset, value)
        }
    }

    fun notifyCharacteristicChanged(
        device: ClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        when (device) {
            is MockClientDevice -> mockEngine.notifyCharacteristicChanged(device, characteristic, confirm, value)
            is RealClientDevice -> notifyCharacteristicChanged(device, characteristic, confirm, value)
        }
    }

    private fun notifyCharacteristicChanged(
        device: RealClientDevice,
        characteristic: BluetoothGattCharacteristic,
        confirm: Boolean,
        value: ByteArray
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            server.notifyCharacteristicChanged(device.device, characteristic, confirm, value)
        } else {
            characteristic.value = value
            server.notifyCharacteristicChanged(device.device, characteristic, confirm)
        }
    }

    fun close() {
        server.close() //TODO
    }

    fun connect(device: ClientDevice, autoConnect: Boolean) {
        when (device) {
            is MockClientDevice -> mockEngine.connect(device, autoConnect)
            is RealClientDevice -> server.connect(device.device, autoConnect)
        }
    }

    fun readPhy(device: ClientDevice) {
        when (device) {
            is MockClientDevice -> mockEngine.readPhy(device)
            is RealClientDevice -> readPhy(device)
        }
    }

    private fun readPhy(device: RealClientDevice) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.readPhy(device.device)
        } else {
            callback.onEvent(OnPhyRead(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }

    fun requestPhy(device: ClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        when (device) {
            is MockClientDevice -> mockEngine.requestPhy(device, txPhy, rxPhy, phyOption)
            is RealClientDevice -> requestPhy(device, txPhy, rxPhy, phyOption)
        }
    }

    private fun requestPhy(device: RealClientDevice, txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            server.setPreferredPhy(device.device, txPhy.value, rxPhy.value, phyOption.value)
        } else {
            callback.onEvent(OnPhyUpdate(device, BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS))
        }
    }
}
