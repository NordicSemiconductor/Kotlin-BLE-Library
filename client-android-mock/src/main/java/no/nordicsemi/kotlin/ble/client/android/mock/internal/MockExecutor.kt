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

package no.nordicsemi.kotlin.ble.client.android.mock.internal

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.kotlin.ble.client.GattEvent
import no.nordicsemi.kotlin.ble.client.RemoteService
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.core.PeripheralType
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpec
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyOption

/**
 * A mock implementation of [Peripheral] for Android.
 *
 * @param peripheralSpec The peripheral specification.
 * @param name Name of the peripheral read from the advertisement data or peripheral spec
 * when it was connected / bonded before.
 */
open class MockExecutor(
    private val peripheralSpec: PeripheralSpec<String>,
    name: String?,
): Peripheral.Executor {
    override val type: PeripheralType = peripheralSpec.type
    override val initialState: ConnectionState = ConnectionState.Disconnected()
    override val initialServices: List<RemoteService> = emptyList()

    override val identifier: String = peripheralSpec.identifier

    /** The current bond state. */
    private val _bondState = MutableStateFlow(
        if (peripheralSpec.isBonded) BondState.BONDED else BondState.NONE
    )
    override val bondState = _bondState.asStateFlow()

    /**
     * A flag set when the phone connects to the peripheral.
     *
     * We assume, that the Device Name characteristic is read during service discovery.
     */
    private var isNameCached = false

    /** The peripheral name. */
    private var _name: String? = name
    override val name: String?
        get() = if (isNameCached) peripheralSpec.name else _name

    // Implementation

    override val events: SharedFlow<GattEvent>
        get() = TODO("Not yet implemented")

    override val isClosed: Boolean
        get() = TODO("Not yet implemented")

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        TODO("Not yet implemented")
    }

    override fun discoverServices(): Boolean {
        TODO("Not yet implemented")

        // TODO set isNameCached to true when service discovery finished
    }

    override fun createBond(): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeBond(): Boolean {
        TODO("Not yet implemented")
    }

    override fun refreshCache(): Boolean {
        TODO("Not yet implemented")
    }

    override fun requestConnectionPriority(priority: ConnectionPriority): Boolean {
        TODO("Not yet implemented")
    }

    override fun requestMtu(mtu: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption): Boolean {
        TODO("Not yet implemented")
    }

    override fun readPhy(): Boolean {
        TODO("Not yet implemented")
    }

    override fun readRssi(): Boolean {
        TODO("Not yet implemented")
    }

    override fun disconnect(): Boolean {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}