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

package no.nordicsemi.android.kotlin.ble.client.main.service

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.common.core.toDisplayString
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.client.api.DescriptorEvent
import no.nordicsemi.android.kotlin.ble.client.api.OnDescriptorRead
import no.nordicsemi.android.kotlin.ble.client.api.OnDescriptorWrite
import no.nordicsemi.android.kotlin.ble.core.provider.MtuProvider
import no.nordicsemi.android.kotlin.ble.client.main.errors.GattOperationException
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPermission
import no.nordicsemi.android.kotlin.ble.logger.BlekLogger
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattDescriptor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class BleGattDescriptor internal constructor(
    private val gatt: GattClientAPI,
    private val characteristicInstanceId: Int,
    private val descriptor: IBluetoothGattDescriptor,
    private val logger: BlekLogger,
    private val mutex: MutexWrapper,
    private val mtuProvider: MtuProvider
) {

    val uuid = descriptor.uuid

    val permissions = BleGattPermission.createPermissions(descriptor.permissions)

    private var pendingReadEvent: ((OnDescriptorRead) -> Unit)? = null
    private var pendingWriteEvent: ((OnDescriptorWrite) -> Unit)? = null

    internal fun onEvent(event: DescriptorEvent) {
        when (event) {
            is OnDescriptorRead -> onLocalEvent(event.descriptor) { pendingReadEvent?.invoke(event) }
            is OnDescriptorWrite -> onLocalEvent(event.descriptor) { pendingWriteEvent?.invoke(event) }
        }
    }

    private fun onLocalEvent(eventDescriptor: IBluetoothGattDescriptor, block: () -> Unit) {
        if (eventDescriptor.uuid == descriptor.uuid && eventDescriptor.characteristic.instanceId == characteristicInstanceId) {
            block()
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: ByteArray) {
        mutex.lock()
        suspendCoroutine { continuation ->
            logger.log(Log.DEBUG, "Write to descriptor - start, uuid: $uuid, value: ${value.toDisplayString()}")
            pendingWriteEvent = {
                pendingWriteEvent = null
                if (it.status.isSuccess) {
                    logger.log(Log.DEBUG, "Write to descriptor - end, uuid: $uuid, value: ${it.status}")
                    continuation.resume(Unit)
                } else {
                    logger.log(Log.ERROR, "Write to descriptor - error, uuid: $uuid, result: ${it.status}")
                    continuation.resumeWithException(GattOperationException(it.status))
                }
                mutex.unlock()
            }

            gatt.writeDescriptor(descriptor, value)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read(): ByteArray {
        mutex.lock()
        return suspendCoroutine { continuation ->
            logger.log(Log.DEBUG, "Read from descriptor - start, uuid: $uuid")
            pendingReadEvent = {
                pendingReadEvent = null
                if (it.status.isSuccess) {
                    logger.log(Log.DEBUG, "Read from descriptor - end, uuid: $uuid, value: ${it.value}")
                    continuation.resume(it.value.copyOf())
                } else {
                    logger.log(Log.ERROR, "Read from descriptor - error, uuid: $uuid, result: ${it.status}")
                    continuation.resumeWithException(GattOperationException(it.status))
                }
                mutex.unlock()
            }
            gatt.readDescriptor(descriptor)
        }
    }
}
