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

package no.nordicsemi.kotlin.ble.client.mock.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpec
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.ConnectionState.Disconnected.Reason
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.log.Layer
import no.nordicsemi.kotlin.ble.core.mock.MockEnvironment
import no.nordicsemi.kotlin.log.Log
import kotlin.uuid.Uuid

/**
 * A mock implementation of [Peripheral].
 *
 * @param peripheralSpec The peripheral specification.
 * @param name Name of the peripheral read from the advertisement data or peripheral spec
 * when it was connected / bonded before.
 * @param environment The mock environment.
 * @param advertisements A flow of advertisements emitted by the mock advertiser.
 * @hide
 */
open class MockExecutor(
    val peripheralSpec: PeripheralSpec<String>,
    name: String?,
    private val environment: MockEnvironment,
    private val advertisements: Flow<MockScanResult<String>>,
): Peripheral.Executor<String> {
    override var logger: Log.Sink<Layer>? = Log.Sink.Null
    override val initialState: ConnectionState = ConnectionState.Disconnected()
    override val initialServices: List<RemoteService> = emptyList()

    override val identifier: String = peripheralSpec.identifier

    override var isReliableWriteEnabled: Boolean = false

    /** The peripheral name. */
    private var _name: String? = name
    override val name: String?
        // Return the name read from Device Name characteristic if services have been discovered,
        // otherwise return the name from advertisement.
        get() = if (gatt?.cachedServices != null) peripheralSpec.name else _name

    // Implementation

    /** The mock GATT connection. */
    protected var gatt: PeripheralSpec<*>.Api? = null
        private set

    override val events: SharedFlow<GattEvent>
        get() = peripheralSpec.events

    override val isClosed: Boolean
        get() = gatt == null

    override suspend fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        gatt = peripheralSpec.connectGatt(environment, autoConnect, preferredPhy, advertisements)
    }

        override suspend fun discoverServices(uuids: List<Uuid>): Boolean {
        return gatt?.discoverServices(uuids) ?: false
    }

    override suspend fun readRssi(): Boolean {
        return gatt?.readRssi() ?: false
    }

    override suspend fun disconnect(reason: Reason): Boolean {
        gatt?.let { gatt ->
            gatt.disconnect(reason)
            return true
        }
        return false
    }

    override fun close() {
        gatt?.close()
        gatt = null
    }
}