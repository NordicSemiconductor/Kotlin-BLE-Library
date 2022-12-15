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

package no.nordicsemi.android.kotlin.ble.server.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.server.event.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.server.event.OnDescriptorReadRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnDescriptorWriteRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnExecuteWrite
import no.nordicsemi.android.kotlin.ble.server.event.OnMtuChanged

@SuppressLint("MissingPermission")
class BleServerGattDescriptor(
    private val server: BluetoothGattServer,
    private val characteristicInstanceId: Int,
    private val descriptor: BluetoothGattDescriptor
) {

    val uuid = descriptor.uuid

    private var transactionalValue = byteArrayOf()
    private val _value = MutableStateFlow(byteArrayOf())
    val value = _value.asStateFlow()

    private var mtu = BleGattConsts.MIN_MTU

    internal fun onEvent(event: DescriptorEvent) {
        Log.d("AAATESTAAA", "Descriptor event: $event")
        when (event) {
            is OnDescriptorReadRequest -> onLocalEvent(event.descriptor) { onDescriptorReadRequest(event) }
            is OnDescriptorWriteRequest -> onLocalEvent(event.descriptor) { onDescriptorWriteRequest(event) }
            is OnExecuteWrite -> onExecuteWrite(event)
            is OnMtuChanged -> mtu = event.mtu
        }
    }

    fun setValue(value: ByteArray) {
        _value.value = value
    }

    private fun onLocalEvent(eventDescriptor: BluetoothGattDescriptor, block: () -> Unit) {
        //todo add instance id
//        if (eventDescriptor.uuid == descriptor.uuid && eventDescriptor.characteristic.instanceId == characteristicInstanceId) {
        if (eventDescriptor.uuid == descriptor.uuid) {
            block()
        }
    }

    private fun onExecuteWrite(event: OnExecuteWrite) {
        _value.value = transactionalValue
        transactionalValue = byteArrayOf()
        server.sendResponse(event.device, event.requestId, BleGattOperationStatus.GATT_SUCCESS.value, 0, null)
    }

    private fun onDescriptorWriteRequest(event: OnDescriptorWriteRequest) {
        Log.d("AAATESTAAA", "onDescriptorWriteRequest()")
        val status = BleGattOperationStatus.GATT_SUCCESS
        if (event.preparedWrite) {
            transactionalValue += event.value
        } else {
            _value.value = event.value
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
        Log.d("AAATESTAAA", "onDescriptorReadRequest()")
        val status = BleGattOperationStatus.GATT_SUCCESS
        val offset = event.offset
        val maxSize = mtu - 3
        val data = _value.value.getChunk(offset, maxSize)
        Log.d("AAATESTAAA", "descriptor read data: ${data.toReadableString()}")
        server.sendResponse(event.device, event.requestId, status.value, event.offset, data)
    }
}
