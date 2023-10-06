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

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.os.Build
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.*
import no.nordicsemi.android.kotlin.ble.core.RealServerDevice
import no.nordicsemi.android.kotlin.ble.core.ServerDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.PhyOption
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.wrapper.NativeBluetoothGattDescriptor
import java.lang.reflect.Method

/**
 * A wrapper around [BluetoothGatt] and [BluetoothGattCallback]. As an input it uses methods of
 * [BluetoothGatt] and as an output callbacks from [BluetoothGattCallback].
 *
 * @property gatt Native Android API ([BluetoothGatt]) for BLE calls.
 * @property callback Native wrapper around Android [BluetoothGattCallback].
 * @property autoConnect Boolean value passed during connection.
 */
@Suppress("InlinedAPI")
class NativeClientBleAPI(
    private val gatt: BluetoothGatt,
    private val callback: ClientBleGattCallback,
    override val autoConnect: Boolean
) : GattClientAPI {

    override val event: SharedFlow<ClientGattEvent> = callback.event

    override val device: ServerDevice
        get() = RealServerDevice(gatt.device)

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    override fun onEvent(event: ClientGattEvent) {
        callback.onEvent(event)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeCharacteristic(
        characteristic: IBluetoothGattCharacteristic,
        value: DataByteArray,
        writeType: BleWriteType
    ) {
        val c = (characteristic as NativeBluetoothGattCharacteristic).characteristic
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(c, value.value, writeType.value)
        } else @Suppress("DEPRECATION") {
            c.writeType = writeType.value
            c.value = value.value
            gatt.writeCharacteristic(c)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readCharacteristic(
        characteristic: IBluetoothGattCharacteristic
    ) {
        val c = (characteristic as NativeBluetoothGattCharacteristic).characteristic
        gatt.readCharacteristic(c)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun enableCharacteristicNotification(characteristic: IBluetoothGattCharacteristic) {
        val c = (characteristic as NativeBluetoothGattCharacteristic).characteristic
        gatt.setCharacteristicNotification(c, true)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disableCharacteristicNotification(characteristic: IBluetoothGattCharacteristic) {
        val c = (characteristic as NativeBluetoothGattCharacteristic).characteristic
        gatt.setCharacteristicNotification(c, false)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun writeDescriptor(descriptor: IBluetoothGattDescriptor, value: DataByteArray) {
        val d = (descriptor as NativeBluetoothGattDescriptor).descriptor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(d, value.value)
        } else @Suppress("DEPRECATION") {
            d.value = value.value
            gatt.writeDescriptor(d)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readDescriptor(descriptor: IBluetoothGattDescriptor) {
        val d = (descriptor as NativeBluetoothGattDescriptor).descriptor
        gatt.readDescriptor(d)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun requestMtu(@IntRange(from = 23, to = 517) mtu: Int) {
        gatt.requestMtu(mtu)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readRemoteRssi() {
        gatt.readRemoteRssi()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun readPhy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt.readPhy()
        } else {
            callback.onEvent(
                PhyUpdate(BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS)
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun discoverServices() {
        gatt.discoverServices()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun setPreferredPhy(txPhy: BleGattPhy, rxPhy: BleGattPhy, phyOption: PhyOption) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            gatt.setPreferredPhy(txPhy.value, rxPhy.value, phyOption.value)
        } else {
            callback.onEvent(
                PhyUpdate(BleGattPhy.PHY_LE_1M, BleGattPhy.PHY_LE_1M, BleGattOperationStatus.GATT_SUCCESS)
            )
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun disconnect() {
        gatt.disconnect()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun clearServicesCache() {
        try {
            val refreshMethod: Method = gatt.javaClass.getMethod("refresh")
            refreshMethod.invoke(gatt)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun close() {
        gatt.close()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun beginReliableWrite() {
        gatt.beginReliableWrite()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun abortReliableWrite() {
        gatt.abortReliableWrite()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun executeReliableWrite() {
        gatt.executeReliableWrite()
    }
}
