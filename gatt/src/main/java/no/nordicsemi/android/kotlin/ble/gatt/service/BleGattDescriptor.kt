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

package no.nordicsemi.android.kotlin.ble.gatt.service

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import androidx.annotation.RequiresPermission
import no.nordicsemi.android.kotlin.ble.gatt.event.CharacteristicEvent
import no.nordicsemi.android.kotlin.ble.gatt.event.OnDescriptorRead
import no.nordicsemi.android.kotlin.ble.gatt.event.OnDescriptorWrite
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BleGattDescriptor(private val gatt: BluetoothGatt, private val descriptor: BluetoothGattDescriptor) {

    val uuid = descriptor.uuid

    val permissions = BleGattPermission.createPermissions(descriptor.permissions)

    private var pendingEvent: ((CharacteristicEvent) -> Unit)? = null

    internal fun onEvent(event: CharacteristicEvent) {
        pendingEvent?.invoke(event)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun write(value: ByteArray) = suspendCoroutine { continuation ->
        pendingEvent = { it.onWriteEvent { continuation.resume(Unit) } }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeDescriptor(descriptor, value)
        } else {
            descriptor.value = value
            gatt.writeDescriptor(descriptor)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun read() = suspendCoroutine { continuation ->
        pendingEvent = { it.onReadEvent { continuation.resume(Unit) } }
        gatt.readDescriptor(descriptor)
    }

    private fun CharacteristicEvent.onWriteEvent(onSuccess: () -> Unit) {
        (this as? OnDescriptorWrite)?.let {
            if (it.descriptor == descriptor) {
                onSuccess()
            }
        }
    }

    private fun CharacteristicEvent.onReadEvent(onSuccess: (ByteArray) -> Unit) {
        (this as? OnDescriptorRead)?.let {
            if (it.descriptor == descriptor) {
                onSuccess(it.value)
            }
        }
    }
}
