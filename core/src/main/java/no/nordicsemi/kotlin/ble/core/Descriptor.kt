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

import no.nordicsemi.kotlin.ble.core.util.BluetoothUuid
import java.util.UUID

/**
 * Interface representing a Bluetooth GATT descriptor.
 */
interface Descriptor {

    companion object {
        /** Characteristic Extended Properties descriptor UUID. */
        val CHAR_EXT_PROP_UUID: UUID by lazy { BluetoothUuid.uuid(0x2900) }
        /** Characteristic User Description descriptor UUID. */
        val CHAR_USER_DESC_UUID: UUID by lazy { BluetoothUuid.uuid(0x2901) }
        /** Client Characteristic Configuration descriptor UUID. */
        val CLIENT_CHAR_CONF_UUID: UUID by lazy { BluetoothUuid.uuid(0x2902) }
        /** Server Characteristic Configuration descriptor UUID. */
        val SERVER_CHAR_CONF_UUID: UUID by lazy { BluetoothUuid.uuid(0x2903) }
        /** Characteristic Presentation Format descriptor UUID. */
        val CHAR_PRESENTATION_FORMAT_UUID: UUID by lazy { BluetoothUuid.uuid(0x2904) }
        /** Characteristic Aggregate Format descriptor UUID. */
        val CHAR_AGGREGATE_FORMAT_UUID: UUID by lazy { BluetoothUuid.uuid(0x2905) }
    }

    /**
     * The owner of the parent service of this descriptor.
     *
     * The owner is set to null when the service was invalidated.
     */
    val owner: Peer<*>?

    /**
     * [UUID] of the descriptor.
     */
    val uuid: UUID

    /**
     * Instance id of the descriptor.
     */
    val instanceId: Int

    /**
     * The parent characteristic.
     */
    val characteristic: Characteristic<*>

    /**
     * Returns true if the descriptor is a Client Characteristic Configuration descriptor.
     */
    val isClientCharacteristicConfiguration: Boolean
        get() = uuid == CLIENT_CHAR_CONF_UUID
}
