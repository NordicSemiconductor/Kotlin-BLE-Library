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

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.takeWhile
import no.nordicsemi.kotlin.ble.client.AnyRemoteService
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.exception.InvalidAttributeException
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.Descriptor
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.WriteType
import no.nordicsemi.kotlin.ble.core.exception.BluetoothException
import java.util.UUID

internal class NativeRemoteCharacteristic(
    parent: AnyRemoteService,
    private val gatt: BluetoothGatt,
    private val characteristic: BluetoothGattCharacteristic,
    private val events: Flow<GattEvent>,
): RemoteCharacteristic {
    override val service: AnyRemoteService = parent
    override val uuid: UUID = characteristic.uuid
    override val instanceId: Int = characteristic.instanceId
    override val properties: List<CharacteristicProperty> = characteristic.properties.toList()
    override val descriptors: List<RemoteDescriptor> = characteristic.descriptors.map {
        NativeRemoteDescriptor(this, gatt, it, events)
    }

    @Suppress("DEPRECATION")
    override val isNotifying: Boolean
        // Check the value of the CCCD descriptor, if such exists.
        get() = owner != null && characteristic.getDescriptor(Descriptor.CLIENT_CHAR_CONF_UUID)
            ?.value
            // The CCCD value is 2 bytes long: 0x01-00 for notifications, 0x02-00 for indications.
            ?.let { it.size == 2 && it[0].toInt() and 0b11 != 0 }
            // If the CCCD does not exist, notifications or indications cannot be enabled.
            ?: false

    override suspend fun setNotifying(enabled: Boolean) {
        // Check whether the characteristic wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        // If the current state of notifications is the same as the requested state, return.
        if (enabled == isNotifying)
            return

        // Verify that the characteristic can be subscribed to.
        require(properties.intersect(listOf(CharacteristicProperty.NOTIFY, CharacteristicProperty.INDICATE)).isNotEmpty()) {
            throw OperationFailedException(OperationStatus.SUBSCRIBE_NOT_PERMITTED)
        }

        // Check if the CCCD descriptor exists.
        val descriptor = descriptors
            .firstOrNull { it.isClientCharacteristicConfiguration }
            ?: throw OperationFailedException(OperationStatus.SUBSCRIBE_NOT_PERMITTED)

        // Enable handling of notifications or indications locally.
        val success = try {
            gatt.setCharacteristicNotification(characteristic, enabled)
        } catch (e: Exception) {
            throw BluetoothException(e)
        }
        check(success) {
            throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
        }

        // Enable notifications or indications by writing to the CCCD descriptor.
        val value = when {
            enabled && CharacteristicProperty.INDICATE in properties -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            enabled -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
        }
        descriptor.write(value)
    }

    override suspend fun read(): ByteArray {
        // Check whether the characteristic wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        // Verify that the characteristic can be read.
        require(CharacteristicProperty.READ in properties) {
            throw OperationFailedException(OperationStatus.READ_NOT_PERMITTED)
        }

        return NativeOperationMutex.withLock {
            // Read the characteristic value.
            val success = try {
                gatt.readCharacteristic(characteristic)
            } catch (e: Exception) {
                throw BluetoothException(e)
            }
            check(success) {
                throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
            }

            // Await the response.
            events
                .takeWhile { !it.isServiceInvalidatedEvent }
                .filterIsInstance(CharacteristicRead::class)
                .filter { it.characteristic == characteristic }
                .firstOrNull()
                ?.let {
                    when (it.status) {
                        OperationStatus.SUCCESS -> it.value
                        else -> throw OperationFailedException(it.status)
                    }
                }
                ?: throw InvalidAttributeException()
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun write(data: ByteArray, writeType: WriteType) {
        // Check whether the characteristic wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        // Verify that the characteristic can be written.
        require(properties.intersect(listOf(CharacteristicProperty.WRITE, CharacteristicProperty.WRITE_WITHOUT_RESPONSE)).isNotEmpty()) {
            throw OperationFailedException(OperationStatus.WRITE_NOT_PERMITTED)
        }

        // Write the characteristic value.
        NativeOperationMutex.withLock {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val result = gatt.writeCharacteristic(characteristic, data, writeType.toInt())
                when (result) {
                    BluetoothStatusCodes.SUCCESS -> { /* no-op */
                    }

                    BluetoothStatusCodes.ERROR_GATT_WRITE_NOT_ALLOWED -> throw OperationFailedException(
                        OperationStatus.WRITE_NOT_PERMITTED
                    )

                    BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY -> throw OperationFailedException(
                        OperationStatus.BUSY
                    )

                    else -> throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            } else {
                val success = try {
                    characteristic.value = data
                    characteristic.writeType = writeType.toInt()
                    gatt.writeCharacteristic(characteristic)
                } catch (e: Exception) {
                    throw BluetoothException(e)
                }
                check(success) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }

            // Await the write operation result.
            events
                .takeWhile { !it.isServiceInvalidatedEvent }
                .filterIsInstance(CharacteristicWrite::class)
                .filter { it.characteristic == characteristic }
                .firstOrNull()
                ?.let {
                    check(it.status == OperationStatus.SUCCESS) {
                        throw OperationFailedException(it.status)
                    }
                }
                ?: throw InvalidAttributeException()
        }
    }

    override suspend fun subscribe(): Flow<ByteArray> {
        // Check whether the characteristic wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        setNotifying(true)
        return events
            .takeWhile { !it.isServiceInvalidatedEvent }
            .filterIsInstance(CharacteristicChanged::class)
            .filter { it.characteristic == characteristic }
            .map { it.value }
    }

    override suspend fun waitForValueChange(): ByteArray {
        // Check whether the characteristic wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        return subscribe()
            .firstOrNull()
            ?: throw InvalidAttributeException()
    }

    override fun toString(): String = uuid.toString()
}

private fun Int.toList(): List<CharacteristicProperty> {
    val list = mutableListOf<CharacteristicProperty>()
    if (this and BluetoothGattCharacteristic.PROPERTY_BROADCAST != 0) {
        list.add(CharacteristicProperty.BROADCAST)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS != 0) {
        list.add(CharacteristicProperty.EXTENDED_PROPERTIES)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
        list.add(CharacteristicProperty.INDICATE)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
        list.add(CharacteristicProperty.NOTIFY)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
        list.add(CharacteristicProperty.READ)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE != 0) {
        list.add(CharacteristicProperty.SIGNED_WRITE)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
        list.add(CharacteristicProperty.WRITE)
    }
    if (this and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) {
        list.add(CharacteristicProperty.WRITE_WITHOUT_RESPONSE)
    }
    return list
}