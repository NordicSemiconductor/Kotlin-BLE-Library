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
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.os.Build
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.server.event.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.server.event.OnCharacteristicReadRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnCharacteristicWriteRequest
import no.nordicsemi.android.kotlin.ble.server.event.OnExecuteWrite
import no.nordicsemi.android.kotlin.ble.server.event.OnNotificationSent
import no.nordicsemi.android.kotlin.ble.server.event.ServiceEvent
import java.util.*

@SuppressLint("MissingPermission")
class BleServerGattCharacteristic(
    private val server: BluetoothGattServer,
    private val characteristic: BluetoothGattCharacteristic
) {

    val uuid = characteristic.uuid
    val instanceId = characteristic.instanceId

    private var transactionalValue = byteArrayOf()
    private var value = byteArrayOf()

    private val descriptors = characteristic.descriptors.map {
        BleServerGattDescriptor(server, instanceId, it)
    }

    fun findDescriptor(uuid: UUID): BleServerGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    internal fun onEvent(event: ServiceEvent) {
        when (event) {
            is OnCharacteristicReadRequest -> onLocalEvent(event.characteristic) { onCharacteristicReadRequest(event) }
            is OnCharacteristicWriteRequest -> onLocalEvent(event.characteristic) { onCharacteristicWriteRequest(event) }
            is OnExecuteWrite -> onExecuteWrite(event)
            is OnNotificationSent -> onNotificationSent(event)
            is DescriptorEvent -> descriptors.forEach { it.onEvent(event) }
        }
    }

    private fun onLocalEvent(eventCharacteristic: BluetoothGattCharacteristic, block: () -> Unit) {
        if (eventCharacteristic.uuid == characteristic.uuid && eventCharacteristic.instanceId == characteristic.instanceId) {
            block()
        }
    }

    private fun onExecuteWrite(event: OnExecuteWrite) {
        value = transactionalValue
        transactionalValue = byteArrayOf()
        server.sendResponse(event.device, event.requestId, BleGattOperationStatus.GATT_SUCCESS.value, 0, null)
    }

    private fun onNotificationSent(event: OnNotificationSent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            server.notifyCharacteristicChanged(event.device, characteristic, false, value)
        } else {
            server.notifyCharacteristicChanged(event.device, characteristic, false)
        }
    }

    private fun onCharacteristicWriteRequest(event: OnCharacteristicWriteRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        if (event.preparedWrite) {
            transactionalValue += event.value
        } else {
            value = event.value
        }
        server.sendResponse(event.device, event.requestId, status.value, event.offset, event.value)
    }

    private fun onCharacteristicReadRequest(event: OnCharacteristicReadRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        server.sendResponse(event.device, event.requestId, status.value, event.offset, value)
    }
}
