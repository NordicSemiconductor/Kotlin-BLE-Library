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

package no.nordicsemi.android.kotlin.ble.server.main.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattDescriptor
import kotlinx.coroutines.flow.asSharedFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.Mtu
import no.nordicsemi.android.kotlin.ble.core.event.ValueFlow
import no.nordicsemi.android.kotlin.ble.core.provider.MtuProvider
import no.nordicsemi.android.kotlin.ble.server.api.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.server.api.OnDescriptorReadRequest
import no.nordicsemi.android.kotlin.ble.server.api.OnDescriptorWriteRequest
import no.nordicsemi.android.kotlin.ble.server.api.OnExecuteWrite
import no.nordicsemi.android.kotlin.ble.server.api.ServerAPI

@SuppressLint("MissingPermission")
class BleServerGattDescriptor internal constructor(
    private val server: ServerAPI,
    private val characteristicInstanceId: Int,
    private val descriptor: BluetoothGattDescriptor,
    private val mtuProvider: MtuProvider
) {

    val uuid = descriptor.uuid

    private var transactionalValue = byteArrayOf()
    private val _value = ValueFlow.create()
    val value = _value.asSharedFlow()

    internal fun onEvent(event: DescriptorEvent) {
        when (event) {
            is OnDescriptorReadRequest -> onLocalEvent(event.descriptor) { onDescriptorReadRequest(event) }
            is OnDescriptorWriteRequest -> onLocalEvent(event.descriptor) { onDescriptorWriteRequest(event) }
        }
    }

    fun setValue(value: ByteArray) {
        _value.tryEmit(value)
    }

    private fun onLocalEvent(eventDescriptor: BluetoothGattDescriptor, block: () -> Unit) {
        if (eventDescriptor.uuid == descriptor.uuid && eventDescriptor.characteristic.instanceId == characteristicInstanceId) {
            block()
        }
    }

    private fun onExecuteWrite(event: OnExecuteWrite) {
        _value.tryEmit(transactionalValue)
        transactionalValue = byteArrayOf()
        server.sendResponse(event.device, event.requestId, BleGattOperationStatus.GATT_SUCCESS.value, 0, null)
    }

    private fun onDescriptorWriteRequest(event: OnDescriptorWriteRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        if (event.preparedWrite) {
            transactionalValue += event.value
        } else {
            _value.tryEmit(event.value)
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

    private fun onDescriptorReadRequest(event: OnDescriptorReadRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        val offset = event.offset
        val data = _value.value.getChunk(offset, mtuProvider.mtu.value)
        server.sendResponse(event.device, event.requestId, status.value, event.offset, data)
    }
}
