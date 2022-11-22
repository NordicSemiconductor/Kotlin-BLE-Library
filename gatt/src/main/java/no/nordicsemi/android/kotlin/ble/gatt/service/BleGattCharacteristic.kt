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

package no.nordicsemi.android.kotlin.ble.gatt.service

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.gatt.errors.NotificationDescriptorNotFoundException
import no.nordicsemi.android.kotlin.ble.gatt.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.gatt.event.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.gatt.event.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.gatt.event.OnCharacteristicWrite
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleGattCharacteristic(private val gatt: BluetoothGatt, private val characteristic: BluetoothGattCharacteristic) {

    val uuid = characteristic.uuid

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
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun write(value: ByteArray) = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
    }.also {
        gatt.writeCharacteristic(
            characteristic,
            value,
            BleWriteType.DEFAULT.value
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun write(value: ByteArray, writeType: BleWriteType) = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
    }.also {
        gatt.writeCharacteristic(
            characteristic,
            value,
            writeType.value
        )
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read() = suspendCoroutine { continuation ->
        pendingEvent = { it.onReadEvent { continuation.resume(it) } }
    }.also {
        gatt.readCharacteristic(characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun enableIndications() = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
    }.also {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.setCharacteristicNotification(characteristic, true)
            descriptor.write(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        } ?: throw NotificationDescriptorNotFoundException()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun enableNotifications() = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
    }.also {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.setCharacteristicNotification(characteristic, true)
            descriptor.write(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
        } ?: throw NotificationDescriptorNotFoundException()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun disableNotifications() = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
    }.also {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.setCharacteristicNotification(characteristic, false)
            descriptor.write(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        } ?: throw NotificationDescriptorNotFoundException()
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
