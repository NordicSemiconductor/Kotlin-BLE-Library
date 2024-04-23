/*
 * Copyright (c) 2023, Nordic Semiconductor
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

import no.nordicsemi.android.kotlin.ble.client.api.ClientGattEvent
import no.nordicsemi.android.kotlin.ble.client.api.GattClientAPI
import no.nordicsemi.android.kotlin.ble.core.mutex.MutexWrapper
import no.nordicsemi.android.kotlin.ble.core.provider.ConnectionProvider
import no.nordicsemi.android.kotlin.ble.core.wrapper.IBluetoothGattService
import java.util.UUID

/**
 * A class which groups service's characteristics.
 *
 * @property gatt [GattClientAPI] for communication with the server device.
 * @property service Identifier of a service.
 * @property logger Logger class for displaying logs.
 * @property mutex Mutex for synchronising requests.
 * @property connectionProvider For providing MTU value established per connection.
 */
data class ClientBleGattService internal constructor(
    private val gatt: GattClientAPI,
    private val service: IBluetoothGattService,
    private val mutex: MutexWrapper,
    private val connectionProvider: ConnectionProvider
) {

    /**
     * [UUID] of the service.
     */
    val uuid = service.uuid

    @Suppress("MemberVisibilityCanBePrivate")
    val characteristics = service.characteristics.map {
        ClientBleGattCharacteristic(gatt, it, mutex, connectionProvider)
    }

    /**
     * Finds characteristic based on [uuid] and eventually [instanceId].
     *
     * @param uuid An [UUID] of a characteristic.
     * @param instanceId Instance id.
     * @return Characteristic or null if not found.
     */
    fun findCharacteristic(uuid: UUID, instanceId: Int? = null): ClientBleGattCharacteristic? {
        return characteristics.firstOrNull { characteristic ->
            characteristic.uuid == uuid && instanceId?.let { characteristic.instanceId == it } ?: true
        }
    }

    /**
     * Propagates GATT events to all of it's characteristics. Each characteristic and descriptor is
     * responsible to decide if it's the receiver of an event.
     *
     * @param event A GATT event.
     */
    internal fun onEvent(event: ClientGattEvent.ServiceEvent) {
        characteristics.forEach { it.onEvent(event) }
    }
}
