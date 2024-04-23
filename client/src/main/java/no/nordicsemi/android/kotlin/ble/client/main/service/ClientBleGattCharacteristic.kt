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

package no.nordicsemi.android.kotlin.ble.client.main.service

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCallback
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.CharacteristicChanged
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.CharacteristicRead
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.CharacteristicWrite
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.ReliableWriteCompleted
import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent.ServiceEvent
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.errors.DeviceDisconnectedException
import no.nordicsemi.android.kotlin.ble.core.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.core.errors.MissingPropertyException
import no.nordicsemi.android.kotlin.ble.core.errors.NotificationDescriptorNotFoundException
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.mutex.RequestedLockedFeature
import no.nordicsemi.android.kotlin.ble.core.provider.ConnectionProvider
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import org.slf4j.LoggerFactory
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private val ENABLE_NOTIFICATION_VALUE = DataByteArray(byteArrayOf(0x01, 0x00))
private val ENABLE_INDICATION_VALUE = DataByteArray(byteArrayOf(0x02, 0x00))
private val DISABLE_NOTIFICATION_VALUE = DataByteArray(byteArrayOf(0x00, 0x00))

/**
 * A helper class which provides operations which can happen on a GATT characteristic. Its main
 * responsibility is to provide write/read/notify features in a synchronous manner, because
 * simultaneous calls will be ignored by Android API.
 *
 * @property gatt [GattClientAPI] for communication with the server device.
 * @property characteristic Identifier of a characteristic.
 * @property logger Logger class for displaying logs.
 * @property mutex Mutex for synchronising requests.
 * @property connectionProvider For providing MTU value established per connection.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("InlinedApi")
class ClientBleGattCharacteristic internal constructor(
    private val gatt: GattClientAPI,
    private val characteristic: IBluetoothGattCharacteristic,
    private val mutex: MutexWrapper,
    private val connectionProvider: ConnectionProvider,
) {
    private val logger = LoggerFactory.getLogger(ClientBleGattCharacteristic::class.java)

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

    private val _notifications = MutableSharedFlow<DataByteArray>(
        extraBufferCapacity = connectionProvider.bufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Enables and observes notifications/indications of the characteristic. After subscriber is
     * closed the notifications should be disabled.
     *
     * It is suspend function which suspends and waits for result. It will also suspend when other
     * request is already being executed.
     *
     * @throws DeviceDisconnectedException when a BLE device is disconnected.
     *
     * @return [Flow] which emits new bytes on notification.
     */
    @SuppressLint("MissingPermission")
    suspend fun getNotifications(
        bufferSize: Int = 0,
        bufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST,
    ): Flow<DataByteArray> {
        if (!connectionProvider.isConnected) {
            return flow { throw DeviceDisconnectedException() }
        }

        return _notifications
            .apply { if (bufferSize > 0) buffer(bufferSize, bufferOverflow) }
            .also { enableIndicationsOrNotifications() }
            .onEach { log(it) }
            .onCompletion { disableNotificationsIfConnected() }
    }

    val descriptors = characteristic.descriptors.map {
        ClientBleGattDescriptor(
            gatt,
            instanceId,
            it,
            mutex,
            connectionProvider
        )
    }

    private var pendingReadEvent: ((CharacteristicRead) -> Unit)? = null
    private var pendingWriteEvent: ((CharacteristicWrite) -> Unit)? = null

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
            is ReliableWriteCompleted -> {}
        }
    }

    private fun log(data: DataByteArray) {
        logger.info("Notification received: {}", data)
    }

    private fun onEvent(event: CharacteristicEvent) {
        when (event) {
            is CharacteristicChanged -> onLocalEvent(event.characteristic) {
                _notifications.tryEmit(event.value)
            }

            is CharacteristicRead -> onLocalEvent(event.characteristic) {
                pendingReadEvent?.invoke(event)
            }

            is CharacteristicWrite -> onLocalEvent(event.characteristic) {
                pendingWriteEvent?.invoke(event)
            }
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
     * @throws DeviceDisconnectedException when a BLE device is disconnected.
     *
     * @param value Bytes to write.
     * @param writeType Write type method.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: DataByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) {
        if (!connectionProvider.isConnected) {
            throw DeviceDisconnectedException()
        }
        mutex.lock(RequestedLockedFeature.CHARACTERISTIC_WRITE)
        val stacktrace = Exception() //Helper exception to display valid stacktrace.
        return suspendCoroutine { continuation ->
            logger.trace("Writing to characteristic (uuid: {}), value: {}, type: {}", uuid, value, writeType)
            validateWriteProperties(writeType)
            pendingWriteEvent = {
                pendingWriteEvent = null
                if (it.status.isSuccess) {
                    logger.info("Value written to characteristic (uuid: {}) complete", uuid)
                    continuation.resume(Unit)
                } else {
                    logger.error("Writing to characteristic failed (uuid:{}), status: {}", uuid, it.status)
                    continuation.resumeWithException(
                        GattOperationException(
                            it.status,
                            cause = stacktrace
                        )
                    )
                }
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
     * @throws DeviceDisconnectedException when a BLE device is disconnected.
     *
     * @param value Bytes to write.
     * @param writeType Write type method.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun splitWrite(value: DataByteArray, writeType: BleWriteType = BleWriteType.DEFAULT) {
        value.split(connectionProvider.availableMtu(writeType)).forEach {
            write(it, writeType)
        }
    }

    private fun validateWriteProperties(writeType: BleWriteType) {
        when (writeType) {
            BleWriteType.DEFAULT -> if (!properties.contains(BleGattProperty.PROPERTY_WRITE)) {
                mutex.unlock(RequestedLockedFeature.CHARACTERISTIC_WRITE)
                logger.error("Writing to characteristic failed (uuid: {}), missing WRITE property", uuid)
                throw MissingPropertyException(BleGattProperty.PROPERTY_WRITE)
            }

            BleWriteType.NO_RESPONSE -> if (!properties.contains(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)) {
                mutex.unlock(RequestedLockedFeature.CHARACTERISTIC_WRITE)
                logger.error("Writing to characteristic failed (uuid: {}), missing WRITE_NO_RESPONSE property", uuid)
                throw MissingPropertyException(BleGattProperty.PROPERTY_WRITE_NO_RESPONSE)
            }

            BleWriteType.SIGNED -> if (!properties.contains(BleGattProperty.PROPERTY_SIGNED_WRITE)) {
                mutex.unlock(RequestedLockedFeature.CHARACTERISTIC_WRITE)
                logger.error("Writing to characteristic failed (uuid: {}), missing SIGNED_WRITE property", uuid)
                throw MissingPropertyException(BleGattProperty.PROPERTY_SIGNED_WRITE)
            }
        }
    }

    /**
     * Reads value from a characteristic and suspends for the result.
     *
     * @throws MissingPropertyException when [BleGattProperty.PROPERTY_READ] not found on a characteristic.
     * @throws GattOperationException on GATT communication failure.
     * @throws DeviceDisconnectedException when a BLE device is disconnected.
     *
     * @return Read value.
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read(): DataByteArray {
        if (!connectionProvider.isConnected) {
            throw DeviceDisconnectedException()
        }
        mutex.lock(RequestedLockedFeature.CHARACTERISTIC_READ)
        val stacktrace = Exception() //Helper exception to display valid stacktrace.
        return suspendCancellableCoroutine { continuation ->
            logger.trace("Reading from characteristic (uuid: {})", uuid)
            if (!properties.contains(BleGattProperty.PROPERTY_READ)) {
                mutex.unlock(RequestedLockedFeature.CHARACTERISTIC_READ)
                logger.error("Reading from characteristic failed (uuid: {}), missing READ property", uuid)
                throw MissingPropertyException(BleGattProperty.PROPERTY_READ)
            }
            pendingReadEvent = {
                pendingReadEvent = null
                if (it.status.isSuccess) {
                    logger.info("Characteristic value read (uuid: {}), value: {}", uuid, it.value)
                    continuation.resume(it.value.copyOf())
                } else {
                    logger.error("Reading from characteristic failed (uuid: {}), status: {}", uuid, it.status)
                    continuation.resumeWithException(
                        GattOperationException(
                            it.status,
                            cause = stacktrace
                        )
                    )
                }
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
        logger.trace("Enable indications (uuid: {})", uuid)
        return findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.enableCharacteristicNotification(characteristic)
            descriptor.write(ENABLE_INDICATION_VALUE).also {
                logger.info("Indications enabled (uuid: {})", uuid)
            }
        } ?: run {
            logger.error("Enabling indications failed (uuid: {}), missing CCCD descriptor", uuid)
            throw NotificationDescriptorNotFoundException()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun enableNotifications() {
        logger.trace("Enabling notifications (uuid: {})", uuid)
        return findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.enableCharacteristicNotification(characteristic)
            descriptor.write(ENABLE_NOTIFICATION_VALUE).also {
                logger.info("Notifications enabled (uuid: {})", uuid)
            }
        } ?: run {
            logger.error("Enabling notifications failed (uuid: {}), missing CCCD descriptor", uuid)
            throw NotificationDescriptorNotFoundException()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun disableNotificationsIfConnected() {
        if (connectionProvider.isConnected) {
            disableNotifications()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private suspend fun disableNotifications() {
        if (!connectionProvider.isConnected) {
            throw DeviceDisconnectedException()
        }
        logger.trace("Disabling notifications (uuid: {})", uuid)
        return findDescriptor(BleGattConsts.NOTIFICATION_DESCRIPTOR)?.let { descriptor ->
            gatt.disableCharacteristicNotification(characteristic)
            descriptor.write(DISABLE_NOTIFICATION_VALUE).also {
                logger.info("Notifications disabled (uuid: {})", uuid)
            }
        } ?: run {
            logger.error("Disabling notifications failed (uuid: {}), missing CCCD descriptor", uuid)
            throw NotificationDescriptorNotFoundException()
        }
    }
}
