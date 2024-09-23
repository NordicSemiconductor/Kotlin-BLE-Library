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
import android.bluetooth.BluetoothGattService
import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.ble.client.AnyRemoteService
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteCharacteristic
import no.nordicsemi.kotlin.ble.client.RemoteIncludedService
import no.nordicsemi.kotlin.ble.client.RemoteService
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
internal class NativeRemoteService(
    gatt: BluetoothGatt,
    service: BluetoothGattService,
    events: Flow<GattEvent>,
): RemoteService() {
    // NOTE: The owner is set by the GenericPeripheral when handling ServicesChanged event.
    override val uuid: Uuid = service.uuid.toKotlinUuid
    override val instanceId: Int = service.instanceId

    override val characteristics: List<RemoteCharacteristic> = service.characteristics
        .map { NativeRemoteCharacteristic(this, gatt, it, events) }
    override val includedServices: List<NativeRemoteIncludedService> = service.includedServices
        .map { NativeRemoteIncludedService(this, gatt, it, events) }

    override fun toString(): String = uuid.toString()
}

@OptIn(ExperimentalUuidApi::class)
internal class NativeRemoteIncludedService(
    parent: AnyRemoteService,
    gatt: BluetoothGatt,
    service: BluetoothGattService,
    events: Flow<GattEvent>,
): RemoteIncludedService {
    override val service: AnyRemoteService = parent
    override val uuid: Uuid = service.uuid.toKotlinUuid
    override val instanceId: Int = service.instanceId

    override val characteristics: List<RemoteCharacteristic> = service.characteristics
        .map { NativeRemoteCharacteristic(this, gatt, it, events) }
    override val includedServices: List<NativeRemoteIncludedService> = service.includedServices
        .map { NativeRemoteIncludedService(this, gatt, it, events) }

    override fun toString(): String = uuid.toString()
}