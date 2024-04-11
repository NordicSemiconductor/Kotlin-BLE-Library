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

import java.util.UUID

/**
 * Interface representing a Bluetooth GATT descriptor.
 */
interface Descriptor {

    companion object {
        /*
         * Note:
         *
         * What is "-0x7FFFFF7FA064CB05", you ask?
         *
         * Due to the fact, that the least significant part of the Base Bluetooth UUID
         * (0x800000805f9b34fb) is outside of the range of Long, the value cannot be
         * simply written as UUID(0x0000290000001000, 0x800000805f9B34FB).
         * Instead, we take a 2's complement of the least significant part and invert the sign.
         */
        /** Characteristic Extended Properties descriptor UUID. */
        val CHAR_EXT_PROP_UUID: UUID by lazy { UUID(0x0000290000001000, -0x7FFFFF7FA064CB05) }
        /** Characteristic User Description descriptor UUID. */
        val CHAR_USER_DESC_UUID: UUID by lazy { UUID(0x0000290100001000, -0x7FFFFF7FA064CB05) }
        /** Client Characteristic Configuration descriptor UUID. */
        val CLIENT_CHAR_CONF_UUID: UUID by lazy { UUID(0x0000290200001000, -0x7FFFFF7FA064CB05) }
        /** Server Characteristic Configuration descriptor UUID. */
        val SERVER_CHAR_CONF_UUID: UUID by lazy { UUID(0x0000290300001000, -0x7FFFFF7FA064CB05) }
        /** Characteristic Presentation Format descriptor UUID. */
        val CHAR_PRESENTATION_FORMAT_UUID: UUID by lazy { UUID(0x0000290400001000, -0x7FFFFF7FA064CB05) }
        /** Characteristic Aggregate Format descriptor UUID. */
        val CHAR_AGGREGATE_FORMAT_UUID: UUID by lazy { UUID(0x0000290500001000, -0x7FFFFF7FA064CB05) }
    }

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
