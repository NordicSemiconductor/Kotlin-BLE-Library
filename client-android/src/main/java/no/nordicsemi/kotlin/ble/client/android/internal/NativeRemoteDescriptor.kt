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
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothStatusCodes
import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.takeWhile
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteDescriptor
import no.nordicsemi.kotlin.ble.client.exception.InvalidAttributeException
import no.nordicsemi.kotlin.ble.client.exception.OperationFailedException
import no.nordicsemi.kotlin.ble.core.OperationStatus
import no.nordicsemi.kotlin.ble.core.exception.BluetoothException
import java.util.UUID

internal class NativeRemoteDescriptor(
    parent: RemoteCharacteristic,
    private val gatt: BluetoothGatt,
    private val descriptor: BluetoothGattDescriptor,
    private val events: Flow<GattEvent>,
): RemoteDescriptor {
    override val characteristic: RemoteCharacteristic = parent
    override val uuid: UUID = descriptor.uuid
    override val instanceId: Int = descriptor.instanceId

    override suspend fun read(): ByteArray {
        // Check whether the descriptor wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        return NativeOperationMutex.withLock {
            // Read the descriptor value.
            val success = try {
                gatt.readDescriptor(descriptor)
            } catch (e: Exception) {
                throw BluetoothException(e)
            }
            check(success) {
                throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
            }

            events
                .takeWhile { !it.isServiceInvalidatedEvent }
                .filterIsInstance(DescriptorRead::class)
                .filter { it.descriptor == descriptor }
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
    override suspend fun write(data: ByteArray) {
        // Check whether the descriptor wasn't invalidated.
        require(owner != null) {
            throw InvalidAttributeException()
        }

        NativeOperationMutex.withLock {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val result = gatt.writeDescriptor(descriptor, data)
                when (result) {
                    BluetoothStatusCodes.SUCCESS -> { /* no-op */ }
                    BluetoothStatusCodes.ERROR_GATT_WRITE_REQUEST_BUSY ->
                        throw OperationFailedException(OperationStatus.BUSY)
                    else -> throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            } else {
                val success = try {
                    descriptor.value = data
                    // There was a bug on an early versions of Android, where the descriptor
                    // was written using the write type of the parent characteristic.
                    // Instead, descriptors can only be written using WRITE_TYPE_DEFAULT.
                    descriptor.characteristic.writeType =
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    gatt.writeDescriptor(descriptor)
                } catch (e: Exception) {
                    throw BluetoothException(e)
                }
                check(success) {
                    throw OperationFailedException(OperationStatus.UNKNOWN_ERROR)
                }
            }
            events
                .takeWhile { !it.isServiceInvalidatedEvent }
                .filterIsInstance(DescriptorWrite::class)
                .filter { it.descriptor == descriptor }
                .firstOrNull()
                ?.let {
                    check(it.status.isSuccess) {
                        throw OperationFailedException(it.status)
                    }
                }
                ?: throw InvalidAttributeException()
        }
    }

    override fun toString(): String = uuid.toString()
}

private val BluetoothGattDescriptor.instanceId: Int
    @SuppressLint("PrivateApi")
    get() = try {
        val method = BluetoothGattDescriptor::class.java.getDeclaredMethod("getInstanceId")
        method.invoke(this) as Int
    } catch (e: Exception) {
        // Handle the exception or return a default value
        -1 // Assuming -1 is an invalid instance ID and used as an error code
    }