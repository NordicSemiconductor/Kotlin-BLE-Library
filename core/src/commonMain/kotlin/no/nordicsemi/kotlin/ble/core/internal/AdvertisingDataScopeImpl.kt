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

package no.nordicsemi.kotlin.ble.core.internal

import no.nordicsemi.kotlin.ble.core.AdvertisingDataDefinition
import no.nordicsemi.kotlin.ble.core.AdvertisingDataScope
import no.nordicsemi.kotlin.ble.core.util.fromShortUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Implementation of the advertising data scope used to build the [AdvertisingDataDefinition].
 */
@OptIn(ExperimentalUuidApi::class)
open class AdvertisingDataScopeImpl: AdvertisingDataScope {

    /**
     * Returns the advertising data depending on the implementation.
     *
     * If the Service UUIDs were not specified, this method will return `null`.
     */
    open fun build(): AdvertisingDataDefinition = AdvertisingDataDefinition(serviceUuids)

    /**
     * The list of service UUIDs to be advertised.
     *
     * The list may contain UUIDs of 16-bit, 32-bit, or 128-bit length, which will be
     * advertised in the corresponding AD fields.
     */
    protected var serviceUuids: List<Uuid>? = null

    override fun ServiceUuid(uuid: Uuid) {
        serviceUuids = serviceUuids?.plus(uuid) ?: listOf(uuid)
    }

    override fun ServiceUuid(shortUuid: Int) {
        ServiceUuid(Uuid.fromShortUuid(shortUuid))
    }

    open val isEmpty: Boolean
        get() = serviceUuids.isNullOrEmpty()
}