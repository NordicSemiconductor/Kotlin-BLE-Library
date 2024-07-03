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

package no.nordicsemi.kotlin.ble.client

import no.nordicsemi.kotlin.ble.core.ConnectionState

/**
 * A base class for all GATT events.
 */
sealed class GattEvent {

    val isDisconnectionEvent: Boolean
        get() = this is ConnectionStateChanged && disconnected
}

/**
 * Event indicating that the connection state has changed.
 *
 * @param newState The new connection state.
 */
data class ConnectionStateChanged(val newState: ConnectionState) : GattEvent() {

    /** Returns whether the new state is [ConnectionState.Disconnected]. */
    val disconnected: Boolean
        get() = newState is ConnectionState.Disconnected
}

/**
 * Event indicating that the services have changed.
 *
 * @param services The list of discovered remote services.
 */
data class ServicesChanged(val services: List<RemoteService>) : GattEvent()

/**
 * Event indicating that the RSSI value has been read.
 *
 * The RSSI value is the signal strength of the signal received from the peripheral, in dBm.
 * The higher value, the stronger signal. Usually, RSSI is in range -100 dBm (far away) to
 * -30 dBm (very close). The value depends on multiple factors, such as distance, obstacles,
 * antenna, phone orientation, etc.
 *
 * @param rssi The RSSI value.
 */
data class RssiRead(val rssi: Int) : GattEvent()

/**
 * Event type used by implementations.
 */
open class ImplSpecificEvent : GattEvent()
