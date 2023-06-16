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
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.client.api.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.api.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnReliableWriteCompleted
import no.nordicsemi.android.kotlin.ble.client.api.ServiceEvent
import no.nordicsemi.android.kotlin.ble.core.provider.MtuProvider
import no.nordicsemi.android.kotlin.ble.client.main.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.client.main.errors.MissingPropertyException
import no.nordicsemi.android.kotlin.ble.client.main.errors.NotificationDescriptorNotFoundException
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.ext.toDisplayString
import no.nordicsemi.android.common.logger.BlekLogger
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.splitter.split
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BleGattCharacteristic internal constructor(
    private val gatt: GattClientAPI,
    private val characteristic: IBluetoothGattCharacteristic,
    private val logger: no.nordicsemi.android.common.logger.BlekLogger,
    private val mutex: MutexWrapper,
    private val mtuProvider: MtuProvider
) {

    val uuid = characteristic.uuid

    val instanceId = characteristic.instanceId

    val permissions = BleGattPermission.createPermissions(characteristic.permissions)

    val properties = BleGattProperty.createProperties(characteristic.properties)

    private val _notifications = MutableSharedFlow<ByteArray>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    @SuppressLint("MissingPermission")
    suspend fun getNotifications(): Flow<ByteArray> {
        try {
            enableIndicationsOrNotifications()
        } catch (e: Exception) {
            e.printStackTrace()
            return flow { throw e }
        }

        return suspendCoroutine {
            it.resume(_notifications.onEach { log(it) }.onCompletion { disableNotifications() })
        }
    }

    private val descriptors = characteristic.descriptors.map { BleGattDescriptor(gatt, instanceId, it, logger, mutex, mtuProvider) }

    private var pendingReadEvent: ((OnCharacteristicRead) -> Unit)? = null
    private var pendingWriteEvent: ((OnCharacteristicWrite) -> Unit)? = null

    fun findDescriptor(uuid: UUID): BleGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    internal fun onEvent(event: ServiceEvent) {
        when (event) {
            is CharacteristicEvent -> onEvent(event)
            is DescriptorEvent -> descriptors.forEach { it.onEvent(event) }
            is OnReliableWriteCompleted -> TODO()
        }
    }

    private fun log(data: ByteArray) {
        logger.log(Log.VERBOSE, "On notification received: ${data.toDisplayString()}")
    }

    private fun onEvent(event: CharacteristicEvent) {
        when (event) {
            is OnCharacteristicChanged -> onLocalEvent(event.characteristic) { _notifications.tryEmit(event.value) }
            is OnCharacteristicRead -> onLocalEvent(event.characteristic) { pendingReadEvent?.invoke(event) }
            is OnCharacteristicWrite -> onLocalEvent(event.characteristic) { pendingWriteEvent?.invoke(event) }
        }
    }

    private fun onLocalEvent(eventCharacteristic: IBluetoothGattCharacteristic, block: () -> Unit) {
        if (eventCharacteristic.uuid == characteristic.uuid && eventCharacteristic.instanceId == characteristic.instanceId) {
            block()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: ByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) {
        mutex.lock()
        return suspendCoroutine { continuation ->
            logger.log(Log.DEBUG, "Write to characteristic - start, uuid: $uuid, value: ${value.toDisplayString()}, type: $writeType")
            validateWriteProperties(writeType)
            pendingWriteEvent = {
                pendingWriteEvent = null
                if (it.status.isSuccess) {
                    logger.log(Log.INFO, "Value written: ${value.toDisplayString()} to $uuid")
                    continuation.resume(Unit)
                } else {
                    logger.log(Log.ERROR, "Write to characteristic - error, uuid: $uuid, result: ${it.status}")
                    continuation.resumeWithException(GattOperationException(it.status))
                }
                mutex.unlock()
            }
            gatt.writeCharacteristic(characteristic, value, writeType)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun splitWrite(value: ByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) {
        logger.log(Log.DEBUG, "Split write to characteristic - start, uuid: $uuid, value: ${value.toDisplayString()}, type: $writeType")
        value.split(mtuProvider.availableMtu(writeType)).forEach {
            write(it, writeType)
        }
        logger.log(Log.DEBUG, "Split write to characteristic - end, uuid: $uuid")
    }

    private fun validateWriteProperties(writeType: BleWriteType) {
        when (writeType) {
            BleWriteType.DEFAULT -> if (!properties.contains(BleGattProperty.PROPERTY_WRITE)) {
                mutex.unlock()
                logger.log(Log.ERROR, "Write to characteristic - missing property error, uuid: $uuid")
                throw MissingPropertyException(BleGattProperty.PROPERTY_WRITE)
            }
            BleWriteType.NO_RESPONSE -> if (!properties.contains(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)) {
                mutex.unlock()
                logger.log(Log.ERROR, "Write to characteristic - missing property error, uuid: $uuid")
                throw MissingPropertyException(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)
            }
            BleWriteType.SIGNED -> if (!properties.contains(BleGattProperty.PROPERTY_SIGNED_WRITE)) {
                mutex.unlock()
                logger.log(Log.ERROR, "Write to characteristic - missing property error, uuid: $uuid")
                throw MissingPropertyException(BleGattProperty.PROPERTY_SIGNED_WRITE)
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read(): ByteArray {
        mutex.lock()
        return suspendCoroutine { continuation ->
            logger.log(Log.DEBUG, "Read from characteristic - start, uuid: $uuid")
            if (!properties.contains(BleGattProperty.PROPERTY_READ)) {
                mutex.unlock()
                logger.log(Log.ERROR, "Read from characteristic - missing property error, uuid: $uuid")
                throw MissingPropertyException(BleGattProperty.PROPERTY_READ)
            }
            pendingReadEvent = {
                pendingReadEvent = null
                if (it.status.isSuccess) {
                    logger.log(Log.INFO, "Value read: ${it.value.toDisplayString()} from $uuid")
                    continuation.resume(it.value)
                } else {
                    logger.log(Log.ERROR, "Read from characteristic - error, uuid: $uuid, result: ${it.status}")
                    continuation.resumeWithException(GattOperationException(it.status))
                }
                mutex.unlock()
            }
            gatt.readCharacteristic(characteristic)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableIndicationsOrNotifications() {
        return if (properties.contains(BleGattProperty.PROPERTY_NOTIFY)) {
            enableNotifications()
        } else if (properties.contains(BleGattProperty.PROPERTY_INDICATE)) {
            enableIndications()
        } else {
            throw MissingPropertyException(BleGattProperty.PROPERTY_NOTIFY)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableIndications() {
        logger.log(Log.DEBUG, "Enable indications on characteristic - start, uuid: $uuid")
        return findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.enableCharacteristicNotification(characteristic)
            descriptor.write(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE).also {
                logger.log(Log.INFO, "Indications enabled: $uuid")
            }
        } ?: run {
            logger.log(Log.ERROR, "Enable indications on characteristic - missing descriptor error, uuid: $uuid")
            throw NotificationDescriptorNotFoundException()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableNotifications() {
        logger.log(Log.DEBUG, "Enable notifications on characteristic - start, uuid: $uuid")
        return findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.enableCharacteristicNotification(characteristic)
            descriptor.write(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE).also {
                logger.log(Log.INFO, "Notifications enabled: $uuid")
            }
        } ?: run {
            logger.log(Log.ERROR, "Enable notifications on characteristic - missing descriptor error, uuid: $uuid")
            throw NotificationDescriptorNotFoundException()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun disableNotifications() {
        logger.log(Log.DEBUG, "Disable notifications on characteristic - start, uuid: $uuid")
        return findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.disableCharacteristicNotification(characteristic)
            descriptor.write(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE).also {
                logger.log(Log.INFO, "Notifications disabled: $uuid")
            }
        } ?: run {
            logger.log(Log.ERROR, "Disable notifications on characteristic - missing descriptor error, uuid: $uuid")
            throw NotificationDescriptorNotFoundException()
        }
    }
}
