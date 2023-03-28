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

package no.nordicsemi.android.kotlin.ble.client.main.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import no.nordicsemi.android.kotlin.ble.client.api.BleGatt
import no.nordicsemi.android.kotlin.ble.client.api.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.api.DataChangedEvent
import no.nordicsemi.android.kotlin.ble.client.api.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnReliableWriteCompleted
import no.nordicsemi.android.kotlin.ble.client.main.errors.MissingPropertyException
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleGattCharacteristic internal constructor(
    private val gatt: BleGatt,
    private val characteristic: BluetoothGattCharacteristic
) {

    val uuid = characteristic.uuid

    val instanceId = characteristic.instanceId

    val permissions = BleGattPermission.createPermissions(characteristic.permissions)

    val properties = BleGattProperty.createProperties(characteristic.properties)

    private val _notification = MutableSharedFlow<ByteArray>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @SuppressLint("MissingPermission")
    suspend fun getNotifications(): Flow<ByteArray> {
        enableIndicationsOrNotifications()

        return suspendCoroutine {
            it.resume(_notification.onCompletion { disableNotifications() })
        }
    }

    private val descriptors = characteristic.descriptors.map { BleGattDescriptor(gatt, it) }

    private var pendingEvent: ((DataChangedEvent) -> Unit)? = null

    fun findDescriptor(uuid: UUID): BleGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    internal fun onEvent(event: DataChangedEvent) {
        when (event) {
            is CharacteristicEvent -> onEvent(event)
            is DescriptorEvent -> descriptors.forEach { it.onEvent(event) }
            is OnReliableWriteCompleted -> TODO()
        }
    }

    private fun onEvent(event: CharacteristicEvent) {
        if (event.characteristic != characteristic && event.characteristic.instanceId != instanceId) {
            return
        }
        (event as? OnCharacteristicChanged)?.let { _notification.tryEmit(it.value) }
        pendingEvent?.invoke(event)
        pendingEvent = null
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: ByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) = suspendCoroutine { continuation ->
        validateWriteProperties(writeType)
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }
        gatt.writeCharacteristic(characteristic, value, writeType)
    }

    private fun validateWriteProperties(writeType: BleWriteType) {
        when (writeType) {
            BleWriteType.DEFAULT -> if (!properties.contains(BleGattProperty.PROPERTY_WRITE)) {
                throw MissingPropertyException(BleGattProperty.PROPERTY_WRITE)
            }
            BleWriteType.NO_RESPONSE -> if (!properties.contains(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)) {
                throw MissingPropertyException(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)
            }
            BleWriteType.SIGNED -> if (!properties.contains(BleGattProperty.PROPERTY_SIGNED_WRITE)) {
                throw MissingPropertyException(BleGattProperty.PROPERTY_SIGNED_WRITE)
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read() = suspendCoroutine { continuation ->
        if (!properties.contains(BleGattProperty.PROPERTY_READ)) {
            throw MissingPropertyException(BleGattProperty.PROPERTY_READ)
        }
        pendingEvent = { it.onReadEvent { continuation.resume(it) } }
        gatt.readCharacteristic(characteristic)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableIndicationsOrNotifications() {
        if (properties.contains(BleGattProperty.PROPERTY_NOTIFY)) {
            enableNotifications()
        } else if (properties.contains(BleGattProperty.PROPERTY_INDICATE)) {
            enableIndications()
        } else {
            throw MissingPropertyException(BleGattProperty.PROPERTY_NOTIFY)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableIndications() {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.enableCharacteristicNotification(characteristic)
            descriptor.write(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableNotifications() {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.enableCharacteristicNotification(characteristic)
            descriptor.write(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun disableNotifications() {
        findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.disableCharacteristicNotification(characteristic)
            descriptor.write(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
        }
    }

    private fun DataChangedEvent.onWriteEvent(onSuccess: () -> Unit) {
        (this as? OnCharacteristicWrite)?.let {
            if (it.characteristic == characteristic) {
                onSuccess()
            }
        }
    }

    private fun DataChangedEvent.onReadEvent(onSuccess: (ByteArray) -> Unit) {
        (this as? OnCharacteristicRead)?.let {
            if (it.characteristic == characteristic) {
                onSuccess(it.value)
            }
        }
    }
}
