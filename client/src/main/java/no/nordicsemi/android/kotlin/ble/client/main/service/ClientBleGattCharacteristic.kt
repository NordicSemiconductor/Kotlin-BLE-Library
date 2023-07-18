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
import android.bluetooth.BluetoothGattCallback
import android.util.Log
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.api.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.api.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.api.OnCharacteristicWrite
import no.nordicsemi.android.kotlin.ble.client.api.OnReliableWriteCompleted
import no.nordicsemi.android.kotlin.ble.client.api.ServiceEvent
import no.nordicsemi.android.kotlin.ble.client.main.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.client.main.errors.MissingPropertyException
import no.nordicsemi.android.kotlin.ble.client.main.errors.NotificationDescriptorNotFoundException
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.provider.MtuProvider
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.logger.BlekLogger
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val ENABLE_NOTIFICATION_VALUE = DataByteArray(byteArrayOf(0x01, 0x00))
private val ENABLE_INDICATION_VALUE = DataByteArray(byteArrayOf(0x02, 0x00))
private val DISABLE_NOTIFICATION_VALUE = DataByteArray(byteArrayOf(0x00, 0x00))

/**
 * A helper class which provides operation which can happen on a GATT characteristic. It main
 * responsibility is to provide write/read/notify features in a synchronous manner, because
 * simultaneous calls will be ignored by Android API. It has [DataByteArray] value assigned which
 * can change during communication.
 *
 * @property gatt [GattClientAPI] for communication with the server device.
 * @property characteristic Identifier of a characteristic.
 * @property logger Logger class for displaying logs.
 * @property mutex Mutex for synchronising requests.
 * @property mtuProvider For providing mtu value established per connection.
 */
class ClientBleGattCharacteristic internal constructor(
    private val gatt: GattClientAPI,
    private val characteristic: IBluetoothGattCharacteristic,
    private val logger: BlekLogger,
    private val mutex: MutexWrapper,
    private val mtuProvider: MtuProvider,
) {

    /**
     * [UUID] of the characteristic.
     */
    val uuid = characteristic.uuid

    /**
     * Instance id of the characteristic.
     */
    val instanceId = characteristic.instanceId

    /**
     * Permissions of the characteristic.
     */
    val permissions = BleGattPermission.createPermissions(characteristic.permissions)

    /**
     * Properties of the characteristic.
     */
    val properties = BleGattProperty.createProperties(characteristic.properties)

    private val _notifications = MutableSharedFlow<DataByteArray>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Enables and observes notifications/indications of the characteristic. After subscriber is
     * closed the notifications should be disabled.
     *
     * It is suspend function which suspends and waits for result. It will also suspend when other
     * request is already being executed.
     *
     * @return [Flow] which emits new bytes on notification.
     */
    @SuppressLint("MissingPermission")
    suspend fun getNotifications(): Flow<DataByteArray> {
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

    private val descriptors = characteristic.descriptors.map { ClientBleGattDescriptor(gatt, instanceId, it, logger, mutex, mtuProvider) }

    private var pendingReadEvent: ((OnCharacteristicRead) -> Unit)? = null
    private var pendingWriteEvent: ((OnCharacteristicWrite) -> Unit)? = null

    /**
     * Finds a descriptor by [UUID].
     *
     * @param uuid An [UUID] of a descriptor.
     * @return The descriptor or null if not found.
     */
    fun findDescriptor(uuid: UUID): ClientBleGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    /**
     * Consumes events emitted by [BluetoothGattCallback]. Events are emitted everywhere. It is this
     * class responsibility to verify if it's the event destination.
     *
     * @param event A gatt event.
     */
    internal fun onEvent(event: ServiceEvent) {
        when (event) {
            is CharacteristicEvent -> onEvent(event)
            is DescriptorEvent -> descriptors.forEach { it.onEvent(event) }
            is OnReliableWriteCompleted -> { }
        }
    }

    private fun log(data: DataByteArray) {
        logger.log(Log.VERBOSE, "On notification received: $data")
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

    /**
     * Writes bytes to a characteristic and waits for a request to finish.
     *
     * @throws GattOperationException on GATT communication failure.
     * @throws MissingPropertyException when property defined by [writeType] is missing.
     *
     * @param value Bytes to write.
     * @param writeType Write type method.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: DataByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) {
        mutex.lock()
        return suspendCoroutine { continuation ->
            logger.log(Log.DEBUG, "Write to characteristic - start, uuid: $uuid, value: $value, type: $writeType")
            validateWriteProperties(writeType)
            pendingWriteEvent = {
                pendingWriteEvent = null
                if (it.status.isSuccess) {
                    logger.log(Log.INFO, "Value written: $value to $uuid")
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

    /**
     * Writes bytes to a characteristic and waits for a request to finish. If value is bigger than
     * MTU then it splits the value and send it in consecutive messages.
     *
     * @throws GattOperationException on GATT communication failure.
     * @throws MissingPropertyException when property defined by [writeType] is missing.
     *
     * @param value Bytes to write.
     * @param writeType Write type method.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun splitWrite(value: DataByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) {
        logger.log(Log.DEBUG, "Split write to characteristic - start, uuid: $uuid, value: ${value}, type: $writeType")
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

    /**
     * Reads value from a characteristic and suspends for the result.
     *
     * @throws MissingPropertyException when [BleGattProperty.PROPERTY_READ] not found on a characteristic.
     * @throws GattOperationException on GATT communication failure.
     *
     * @return Read value.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read(): DataByteArray {
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
                    logger.log(Log.INFO, "Value read: ${it.value} from $uuid")
                    continuation.resume(it.value.copyOf())
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
            descriptor.write(ENABLE_INDICATION_VALUE).also {
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
            descriptor.write(ENABLE_NOTIFICATION_VALUE).also {
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
            descriptor.write(DISABLE_NOTIFICATION_VALUE).also {
                logger.log(Log.INFO, "Notifications disabled: $uuid")
            }
        } ?: run {
            logger.log(Log.ERROR, "Disable notifications on characteristic - missing descriptor error, uuid: $uuid")
            throw NotificationDescriptorNotFoundException()
        }
    }
}
