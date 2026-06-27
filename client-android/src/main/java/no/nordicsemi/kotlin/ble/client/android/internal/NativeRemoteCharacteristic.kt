/*
 * Copyright (c) 2024, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.client.android.internal

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.kotlin.ble.client.AnyRemoteService
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.exception.InvalidAttributeException
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.client.internal.BaseRemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.internal.OperationEvent
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.log.Layer
import no.nordicsemi.kotlin.log.Log
import kotlin.uuid.Uuid

internal class NativeRemoteCharacteristic(
    parent: AnyRemoteService,
    private val gatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val events: SharedFlow<GattEvent>,
): BaseRemoteCharacteristic(parent, events) {
    override val uuid: Uuid = characteristic.uuid.toKotlinUuid
    override val instanceId: Int = characteristic.instanceId
    override val properties: Set<CharacteristicProperty> = characteristic.properties.toSet()
    override val descriptors: List<RemoteDescriptor> = characteristic.descriptors.map {
        NativeRemoteDescriptor(this, gatt, it, events)
    }

    override fun setCharacteristicNotification(enabled: Boolean) {
        owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
            "gatt.setCharacteristicNotification(${characteristic.uuid}, $enabled)"
        }
        val success = gatt.setCharacteristicNotification(characteristic, enabled)
        check(success) {
            owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
                "Setting characteristic notification failed"
            }
            throw OperationFailedException(OperationStatus.RequestFailed)
        }
    }

    override suspend fun FlowCollector<GattEvent>.executeRead() {
        owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
            "gatt.readCharacteristic(${characteristic.uuid})"
        }
        val success = gatt.readCharacteristic(characteristic)
        check(success) {
            owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
                "Reading characteristic failed"
            }
            throw OperationFailedException(OperationStatus.RequestFailed)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun FlowCollector<GattEvent>.executeWrite(data: ByteArray, writeType: WriteType) {
        owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
            "gatt.writeCharacteristic(${characteristic.uuid}, value=0x${data.toHexString()}, writeType=${writeType.toInt()})"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val result = gatt.writeCharacteristic(characteristic, data, writeType.toInt())

            if (result != BluetoothStatusCodes.SUCCESS) {
                owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
                    "Writing characteristic failed with status $result"
                }
            }

            @SuppressLint("SwitchIntDef")
            when (result) {
                BluetoothStatusCodes.SUCCESS -> { /* no-op */ }

                BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED ->
                    throw OperationFailedException(OperationStatus.WriteNotPermitted)

                BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY ->
                    throw OperationFailedException(OperationStatus.Busy)

                9, /* BluetoothStatusCodes.ERROR_PROFILE_SERVICE_NOT_BOUND */
                28 /* BluetoothStatusCodes.ERROR_CALLBACK_NOT_REGISTERED */ ->
                    throw InvalidAttributeException()

                else ->
                    throw OperationFailedException(OperationStatus.UnknownError(result))
            }
        } else {
            characteristic.value = data
            characteristic.writeType = writeType.toInt()
            val success = gatt.writeCharacteristic(characteristic)
            check(success) {
                owner?.logger?.log(Layer.GATT, Log.Level.DEBUG, owner?.identifier.toString(), null) {
                    "Writing characteristic failed"
                }
                throw OperationFailedException(OperationStatus.RequestFailed)
            }
        }
    }

    override fun OperationEvent.matches(): Boolean = subject == characteristic
}

private fun Int.toSet(): Set<CharacteristicProperty> =
    listOf(
        BluetoothGattCharacteristic.PROPERTY_BROADCAST to CharacteristicProperty.BROADCAST,
        BluetoothGattCharacteristic.PROPERTY_READ to CharacteristicProperty.READ,
        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE to CharacteristicProperty.WRITE_WITHOUT_RESPONSE,
        BluetoothGattCharacteristic.PROPERTY_WRITE to CharacteristicProperty.WRITE,
        BluetoothGattCharacteristic.PROPERTY_NOTIFY to CharacteristicProperty.NOTIFY,
        BluetoothGattCharacteristic.PROPERTY_INDICATE to CharacteristicProperty.INDICATE,
        BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE to CharacteristicProperty.SIGNED_WRITE,
        BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS to CharacteristicProperty.EXTENDED_PROPERTIES,
    ).mapNotNull { (flag, prop) -> if (this and flag != 0) prop else null }
     .toSet()