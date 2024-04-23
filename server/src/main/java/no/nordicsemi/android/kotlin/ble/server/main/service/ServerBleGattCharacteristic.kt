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

package no.nordicsemi.android.kotlin.ble.server.main.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattServerCallback
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.core.errors.MissingPropertyException
import no.nordicsemi.android.kotlin.ble.core.event.ValueFlow
import no.nordicsemi.android.kotlin.ble.core.provider.ConnectionProvider
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattCharacteristic
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.*
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * A helper class which handles operation which can happen on a GATT characteristic on a server
 * side. Its main responsibility is to handle write/read/notify requests in a synchronous manner,
 * because simultaneous calls will be ignored by Android API. It has [DataByteArray] value assigned
 * which can change during communication.
 *
 * @property server [GattServerAPI] for communication with the client device.
 * @property device A client device to which this characteristic belongs.
 * @property characteristic Identifier of a characteristic.
 * @property connectionProvider For providing mtu value established per connection.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
@SuppressLint("MissingPermission")
class ServerBleGattCharacteristic internal constructor(
    private val server: GattServerAPI,
    private val device: ClientDevice,
    private val characteristic: IBluetoothGattCharacteristic,
    private val connectionProvider: ConnectionProvider
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
     * Temporary value used during reliable write operation.
     */
    private var transactionalValue = DataByteArray()

    private var onNotificationSent: ((NotificationSent) -> Unit)? = null

    private val _value = ValueFlow.create(connectionProvider.bufferSize).apply {
        if (characteristic.value != DataByteArray()) { //Don't emit empty value
            this.tryEmit(characteristic.value)
        }
    }

    /**
     * The last value stored on this characteristic.
     */
    val value = _value.asSharedFlow()

    /**
     * Permissions of the characteristic.
     */
    val permissions: List<BleGattPermission>
        get() = BleGattPermission.createPermissions(characteristic.permissions)

    /**
     * Properties of the characteristic.
     */
    val properties: List<BleGattProperty>
        get() = BleGattProperty.createProperties(characteristic.properties)

    val descriptors = characteristic.descriptors.map {
        ServerBleGattDescriptor(server, instanceId, it, connectionProvider)
    }

    /**
     * Finds descriptor of this characteristic based on [uuid].
     *
     * @param uuid Id of descriptor.
     * @return Descriptor or null if not found.
     */
    fun findDescriptor(uuid: UUID): ServerBleGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    /**
     * Sets value for this characteristic. If value has notification/indication property then
     * notification with this value will be send.
     *
     * @param value Bytes to set.
     */
    @Deprecated("Use setLocalValue() instead.")
    fun setValue(value: DataByteArray) {
        _value.tryEmit(value)
        characteristic.value = value
    }

    /**
     * Sets a local value for this characteristic. A notification/indication won't be send.
     *
     * @param value Bytes to set.
     */
    fun setLocalValue(value: DataByteArray) {
        _value.tryEmit(value)
        characteristic.value = value
    }

    /**
     * Sets value for this characteristic and notify the client.
     *
     * @throws MissingPropertyException If a notification property is not set for this characteristic.
     * @throws GattOperationException If sending notification fails.
     *
     * @param value Bytes to set.
     */
    suspend fun setValueAndNotifyClient(value: DataByteArray) {
        val isNotification = properties.contains(BleGattProperty.PROPERTY_NOTIFY)
        val isIndication = properties.contains(BleGattProperty.PROPERTY_INDICATE)

        if (isNotification || isIndication) {
            val stacktrace = Exception() //Helper exception to display valid stacktrace.
            return suspendCancellableCoroutine { continuation ->
                //TODO: Check if this needs Mutex
                onNotificationSent = {
                    onNotificationSent = null
                    if (it.status.isSuccess) {
                        setLocalValue(value)
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(GattOperationException(it.status, stacktrace))
                    }
                }

                server.notifyCharacteristicChanged(device, characteristic, isIndication, value)
            }

        } else {
            throw MissingPropertyException(BleGattProperty.PROPERTY_NOTIFY)
        }
    }

    /**
     * Consumes events emitted by [BluetoothGattServerCallback]. Events are emitted everywhere.
     * It is this class responsibility to verify if it's the event destination.
     *
     * @param event A gatt request.
     */
    internal fun onEvent(event: ServiceEvent) {
        when (event) {
            is CharacteristicEvent -> onCharacteristicEvent(event)
            is DescriptorEvent -> onDescriptorEvent(event)
            is ExecuteWrite -> onExecuteWrite(event)
        }
    }

    /**
     * Verifies if the target of this event is a descriptor belonged to this characteristic.
     * If yes then propagates event to all of its descriptors.
     *
     * @param event A descriptor event.
     */
    private fun onDescriptorEvent(event: DescriptorEvent) {
        val c = event.descriptor.characteristic
        if (c.instanceId == characteristic.instanceId && c.uuid == characteristic.uuid) {
            descriptors.forEach { it.onEvent(event) }
        }
    }

    /**
     * Verifies if the target of this event is this characteristic.
     * If yes then it consumes the request.
     *
     * @param event A characteristic event.
     */
    private fun onCharacteristicEvent(event: CharacteristicEvent) {
        when (event) {
            is CharacteristicReadRequest -> onLocalEvent(event.characteristic) { onCharacteristicReadRequest(event) }
            is CharacteristicWriteRequest -> onLocalEvent(event.characteristic) { onCharacteristicWriteRequest(event) }
            is NotificationSent -> onNotificationSent(event)
        }
    }

    /**
     * Verifies if the target of the event is this characteristic.
     *
     * @param c A characteristic id from the event.
     */
    private fun onLocalEvent(c: IBluetoothGattCharacteristic, block: () -> Unit) {
        if (c.uuid == characteristic.uuid && c.instanceId == characteristic.instanceId) {
            block()
        }
    }

    /**
     * Handles execute write event. It can be either execute or abort request.
     * If abort then temporary value is cleared and previous value will be used.
     * If execute then temporary value will replace previous value.
     *
     * @param event An execute write event.
     */
    private fun onExecuteWrite(event: ExecuteWrite) {
        descriptors.onEach { it.onExecuteWrite(event) }
        if (!event.execute) {
            transactionalValue = DataByteArray()
            return
        }
        if (transactionalValue.size != 0) {
            //we don't send empty value which represent the reliable write does not on this characteristic.
            _value.tryEmit(transactionalValue)
        }
        transactionalValue = DataByteArray()
        server.sendResponse(event.device, event.requestId, BleGattOperationStatus.GATT_SUCCESS.value, 0, null)
    }

    private fun onNotificationSent(event: NotificationSent) {
        onNotificationSent?.invoke(event)
    }

    /**
     * Handles write request. It stores received value in [_value] field.
     * In case of reliable write then the value is stored in a temporary field until
     * [ExecuteWrite] event received.
     * If client used [BleWriteType.DEFAULT] or [BleWriteType.SIGNED] write type then confirmation
     * about received value is sent to client.
     *
     * @param event A write request event.
     */
    private fun onCharacteristicWriteRequest(event: CharacteristicWriteRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        if (event.preparedWrite) {
            transactionalValue = DataByteArray(transactionalValue.value + event.value.value)
        } else {
            _value.tryEmit(event.value.copyOf())
        }
        if (event.responseNeeded) {
            server.sendResponse(
                event.device,
                event.requestId,
                status.value,
                event.offset,
                event.value
            )
        }
    }

    /**
     * Handles read request. It gets value stored in [_value] field and tries to send it. If the
     * size of [DataByteArray] is bigger than mtu value provided by [connectionProvider] then byte array
     * is send in consecutive chunks.
     *
     * @param event A read request event.
     */
    private fun onCharacteristicReadRequest(event: CharacteristicReadRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        val offset = event.offset
        val value = _value.value
        val data = value.getChunk(offset, connectionProvider.mtu.value)
        server.sendResponse(event.device, event.requestId, status.value, event.offset, data)
    }
}
