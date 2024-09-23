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

@file:Suppress("unused")

package no.nordicsemi.kotlin.ble.core

import no.nordicsemi.kotlin.ble.core.util.fromShortUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Interface representing a Bluetooth GATT characteristic.
 */
@OptIn(ExperimentalUuidApi::class)
interface Characteristic<D: Descriptor> {

    companion object {
        /** Device Name characteristic UUID. */
        val DEVICE_NAME: Uuid by lazy { Uuid.fromShortUuid(0x2A00) }
        /** Appearance characteristic UUID. */
        val APPEARANCE: Uuid by lazy { Uuid.fromShortUuid(0x2A01) }
        /** Peripheral Privacy Flag characteristic UUID. */
        val PERIPHERAL_PRIVACY_FLAG: Uuid by lazy { Uuid.fromShortUuid(0x2A02) }
        /** Reconnection Address characteristic UUID. */
        val RECONNECTION_ADDRESS: Uuid by lazy { Uuid.fromShortUuid(0x2A03) }
        /** Peripheral Preferred Connection Parameters characteristic UUID. */
        val PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS: Uuid by lazy { Uuid.fromShortUuid(0x2A04) }
        /** Service Changed characteristic UUID. */
        val SERVICE_CHANGED: Uuid by lazy { Uuid.fromShortUuid(0x2A05) }
    }

    /**
     * The owner of the parent service of this characteristic.
     *
     * The owner is set to null when the service was invalidated.
     */
    val owner: Peer<*>?

    /**
     * [Uuid] of a characteristic.
     */
    val uuid: Uuid

    /**
     * Instance id of a characteristic.
     */
    val instanceId: Int

    /**
     * Characteristic properties.
     */
    val properties: List<CharacteristicProperty>

    /**
     * The parent service.
     */
    val service: AnyService<out Characteristic<D>>

    /**
     * List of descriptors of this characteristic.
     */
    val descriptors: List<D>
}
