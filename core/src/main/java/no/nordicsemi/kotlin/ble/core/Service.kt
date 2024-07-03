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
import no.nordicsemi.kotlin.ble.core.util.BluetoothUuid

/**
 * Interface representing a Bluetooth GATT service.
 */
sealed interface Service<C: Characteristic<*>> {

    companion object {
        /** The UUID of Generic Access Service. */
        val GENERIC_ACCESS_UUID: UUID by lazy { BluetoothUuid.uuid(0x1800) }
        /** The UUID of Generic Attribute Service. */
        val GENERIC_ATTRIBUTE_UUID: UUID by lazy { BluetoothUuid.uuid(0x1801) }
    }

    /**
     * [UUID] of a service.
     */
    val uuid: UUID

    /**
     * Instance id of a characteristic.
     */
    val instanceId: Int

    /**
     * List of characteristics of this service.
     */
    val characteristics: List<C>

    /**
     * List of included secondary services.
     */
    val includedServices: List<IncludedService<C>>
}

/**
 * An interface representing a primary or an included service.
 */
interface AnyService<C: Characteristic<*>>: Service<C> {

    /**
     * The owner of the service.
     *
     * The owner is set to null when the service was invalidated.
     */
    val owner: Peer<*>?
}

/**
 * An interface representing a primary service.
 */
interface PrimaryService<C: Characteristic<*>>: AnyService<C>

/**
 * An interface representing a service included in another service.
 *
 * There are no limits to the number of include definitions or
 * the depth of nested includes in a service definition.
 */
interface IncludedService<C: Characteristic<*>>: AnyService<C> {

    /**
     * The parent service of the service.
     */
    val service: AnyService<C>

    override val owner: Peer<*>?
        get() = service.owner
}
