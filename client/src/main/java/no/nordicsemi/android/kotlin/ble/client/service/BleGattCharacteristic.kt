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

package no.nordicsemi.android.kotlin.ble.client.service

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.client.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.event.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.event.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.event.OnCharacteristicWrite
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleGattCharacteristic(
    private val gatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic
) {

    val uuid = characteristic.uuid

    val instanceId = characteristic.instanceId

    val permissions = BleGattPermission.createPermissions(characteristic.permissions)

    val properties = BleGattProperty.createProperties(characteristic.properties)

    private val _notification = MutableStateFlow(byteArrayOf())
    val notification = _notification.asStateFlow()

    private val descriptors = characteristic.descriptors.map { BleGattDescriptor(gatt, it) }

    private var pendingEvent: ((CharacteristicEvent) -> Unit)? = null

    fun findDescriptor(uuid: UUID): BleGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    internal fun onEvent(event: CharacteristicEvent) {
        event.onNotificationEvent { _notification.value = it }
        pendingEvent?.invoke(event)
        descriptors.forEach { it.onEvent(event) }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: ByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(characteristic, value, writeType.value)
        } else {
            characteristic.writeType = writeType.value
            characteristic.value = value
            gatt.writeCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read() = suspendCoroutine { continuation ->
        pendingEvent = { it.onReadEvent { continuation.resume(it) } }
        gatt.readCharacteristic(characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun enableIndications() {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.setCharacteristicNotification(characteristic, true)
            descriptor.write(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun disableIndications() {
        disableNotifications()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun enableNotifications() {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.setCharacteristicNotification(characteristic, true)
            descriptor.write(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun disableNotifications() {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.setCharacteristicNotification(characteristic, false)
            descriptor.write(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

    private fun CharacteristicEvent.onNotificationEvent(onSuccess: (ByteArray) -> Unit) {
        (this as? OnCharacteristicChanged)?.let {
            if (it.characteristic == characteristic) {
                onSuccess(it.value)
            }
        }
    }

    private fun CharacteristicEvent.onWriteEvent(onSuccess: () -> Unit) {
        (this as? OnCharacteristicWrite)?.let {
            if (it.characteristic == characteristic) {
                onSuccess()
            }
        }
    }

    private fun CharacteristicEvent.onReadEvent(onSuccess: (ByteArray) -> Unit) {
        (this as? OnCharacteristicRead)?.let {
            if (it.characteristic == characteristic) {
                onSuccess(it.value)
            }
        }
    }
}
