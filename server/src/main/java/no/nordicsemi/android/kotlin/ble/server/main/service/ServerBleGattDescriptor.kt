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
import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import no.nordicsemi.android.kotlin.ble.core.event.ValueFlow
import no.nordicsemi.android.kotlin.ble.core.provider.ConnectionProvider
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor
import no.nordicsemi.android.kotlin.ble.server.api.GattServerAPI
import no.nordicsemi.android.kotlin.ble.server.api.ServerGattEvent.*
import java.util.UUID

/**
 * A helper class which handles operation which can happen on a GATT characteristic on a server
 * side. Its main responsibility is to handle write/read requests in a synchronous manner,
 * because simultaneous calls will be ignored by Android API. It has [DataByteArray] value assigned
 * which can change during communication.
 *
 * @property server [GattServerAPI] for communication with the client device.
 * @property characteristicInstanceId Instance id of a parent characteristic.
 * @property descriptor Identifier of a descriptor.
 * @property connectionProvider For providing mtu value established per connection.
 */
@Suppress("unused")
@SuppressLint("MissingPermission")
class ServerBleGattDescriptor internal constructor(
    private val server: GattServerAPI,
    private val characteristicInstanceId: Int,
    private val descriptor: IBluetoothGattDescriptor,
    private val connectionProvider: ConnectionProvider
) {

    /**
     * [UUID] of the descriptor.
     */
    val uuid = descriptor.uuid

    /**
     * Permissions of this descriptor.
     */
    val properties: List<BleGattPermission>
        get() = BleGattPermission.createPermissions(descriptor.permissions)

    private var transactionalValue = DataByteArray()
    private val _value = ValueFlow.create(connectionProvider.bufferSize).apply {
        if (descriptor.value != DataByteArray()) { //Don't emit empty value
            this.tryEmit(descriptor.value)
        }
    }

    /**
     * The last value stored on this descriptor.
     */
    val value = _value.asSharedFlow()

    /**
     * Consumes events emitted by [BluetoothGattServerCallback]. Events are emitted everywhere.
     * It is this class responsibility to verify if it's the event destination.
     *
     * @param event A gatt request.
     */
    internal fun onEvent(event: DescriptorEvent) {
        when (event) {
            is DescriptorReadRequest -> onLocalEvent(event.descriptor) { onDescriptorReadRequest(event) }
            is DescriptorWriteRequest -> onLocalEvent(event.descriptor) { onDescriptorWriteRequest(event) }
        }
    }

    /**
     * Sets new [DataByteArray] value on this descriptor which will be visible for further read
     * requests.
     *
     * @param value New [DataByteArray] value.
     */
    fun setValue(value: DataByteArray) {
        _value.tryEmit(value)
    }

    /**
     * Verifies if the target of the event is this descriptor.
     *
     * @param eventDescriptor A gatt request.
     * @param block A block of code to execute if the target is this descriptor.
     */
    private fun onLocalEvent(eventDescriptor: IBluetoothGattDescriptor, block: () -> Unit) {
        if (eventDescriptor.uuid == descriptor.uuid && eventDescriptor.characteristic.instanceId == characteristicInstanceId) {
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
    internal fun onExecuteWrite(event: ExecuteWrite) {
        if (!event.execute) {
            transactionalValue = DataByteArray()
            return
        }
        if (transactionalValue.size != 0) {
            //we don't send empty value which represent the reliable write does not on this descriptor.
            _value.tryEmit(transactionalValue)
        }
        transactionalValue = DataByteArray()
        server.sendResponse(event.device, event.requestId, BleGattOperationStatus.GATT_SUCCESS.value, 0, null)
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
    private fun onDescriptorWriteRequest(event: DescriptorWriteRequest) {
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
    private fun onDescriptorReadRequest(event: DescriptorReadRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        val offset = event.offset
        val data = _value.value.getChunk(offset, connectionProvider.mtu.value)
        server.sendResponse(event.device, event.requestId, status.value, event.offset, data)
    }
}
