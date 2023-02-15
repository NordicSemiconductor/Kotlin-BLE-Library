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

package no.nordicsemi.android.kotlin.ble.core.server.service.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.ClientDevice
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConsts
import no.nordicsemi.android.kotlin.ble.core.data.BleGattOperationStatus
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.core.data.BleGattProperty
import no.nordicsemi.android.kotlin.ble.core.server.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.core.server.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.core.server.OnCharacteristicReadRequest
import no.nordicsemi.android.kotlin.ble.core.server.OnCharacteristicWriteRequest
import no.nordicsemi.android.kotlin.ble.core.server.OnExecuteWrite
import no.nordicsemi.android.kotlin.ble.core.server.OnMtuChanged
import no.nordicsemi.android.kotlin.ble.core.server.OnNotificationSent
import no.nordicsemi.android.kotlin.ble.core.server.ServiceEvent
import no.nordicsemi.android.kotlin.ble.core.server.api.ServerAPI
import java.util.*

@SuppressLint("MissingPermission")
class BleServerGattCharacteristic internal constructor(
    private val server: ServerAPI,
    private val device: ClientDevice,
    private val characteristic: BluetoothGattCharacteristic
) {

    val uuid = characteristic.uuid
    val instanceId = characteristic.instanceId

    private var transactionalValue = byteArrayOf()

    private val _value = MutableStateFlow(byteArrayOf())
    val value = _value.asStateFlow()

    val permissions: List<BleGattPermission>
        get() = BleGattPermission.createPermissions(characteristic.permissions)

    val properties: List<BleGattProperty>
        get() = BleGattProperty.createProperties(characteristic.properties)

    private var mtu = BleGattConsts.MIN_MTU

    private val descriptors = characteristic.descriptors.map {
        BleServerGattDescriptor(server, instanceId, it)
    }

    fun findDescriptor(uuid: UUID): BleServerGattDescriptor? {
        return descriptors.firstOrNull { it.uuid == uuid }
    }

    fun setValue(value: ByteArray) {
        _value.value = value
        characteristic.value = value

        val isNotification = properties.contains(BleGattProperty.PROPERTY_NOTIFY)
        val isIndication = properties.contains(BleGattProperty.PROPERTY_INDICATE)

        if (isNotification || isIndication) {
            server.notifyCharacteristicChanged(device, characteristic, isIndication, _value.value)
        }
    }

    internal fun onEvent(event: ServiceEvent) {
        (event as? DescriptorEvent)?.let {
            descriptors.forEach { it.onEvent(event) }
        }
        (event as? CharacteristicEvent)?.let {
            when (event) {
                is OnCharacteristicReadRequest -> onLocalEvent(event.characteristic) { onCharacteristicReadRequest(event) }
                is OnCharacteristicWriteRequest -> onLocalEvent(event.characteristic) { onCharacteristicWriteRequest(event) }
                is OnExecuteWrite -> onExecuteWrite(event)
                is OnMtuChanged -> mtu = event.mtu
                is OnNotificationSent -> onNotificationSent(event)
            }
        }
    }

    private fun onLocalEvent(eventCharacteristic: BluetoothGattCharacteristic, block: () -> Unit) {
        //TODO add instance id
//        if (eventCharacteristic.uuid == characteristic.uuid && eventCharacteristic.instanceId == characteristic.instanceId) {
        if (eventCharacteristic.uuid == characteristic.uuid) {
            block()
        }
    }

    private fun onExecuteWrite(event: OnExecuteWrite) {
        _value.value = transactionalValue
        transactionalValue = byteArrayOf()
        server.sendResponse(event.device, event.requestId, BleGattOperationStatus.GATT_SUCCESS.value, 0, null)
    }

    private fun onNotificationSent(event: OnNotificationSent) {
    }

    private fun onCharacteristicWriteRequest(event: OnCharacteristicWriteRequest) {
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

    private fun onCharacteristicReadRequest(event: OnCharacteristicReadRequest) {
        val status = BleGattOperationStatus.GATT_SUCCESS
        val offset = event.offset
        val data = _value.value.getChunk(offset, mtu)
        server.sendResponse(event.device, event.requestId, status.value, event.offset, data)
    }
}
