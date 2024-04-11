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

package no.nordicsemi.kotlin.ble.client.android.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.GattEvent
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.PeripheralType
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyOption

/**
 * A mock implementation of [Peripheral] for Android.
 *
 * @param address The MAC address of the peripheral.
 * @param name An optional name of the peripheral.
 * @param type The type of the peripheral, defaults to [PeripheralType.LE].
 * @param initialBondState The initial bond state of the peripheral, defaults to [BondState.NONE].
 */
open class MockExecutor(
    final override val address: String,
    name: String? = null,
    override val type: PeripheralType = PeripheralType.LE,
    override val initialState: ConnectionState = ConnectionState.Disconnected(),
    initialBondState: BondState = BondState.NONE,
): Peripheral.Executor {

    init {
        // Validate the MAC address.
        require(address.matches(Regex("([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})"))) {
            "Invalid MAC address: $address"
        }
    }

    /** The current bond state. */
    private val _bondState = MutableStateFlow(initialBondState)
    override val bondState = _bondState.asStateFlow()

    /** The peripheral name. */
    private var _name: String? = name
    override val name: String?
        get() = _name

    // Implementation

    override val events: Flow<GattEvent>
        get() = TODO("Not yet implemented")

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        TODO("Not yet implemented")
    }

    override fun requestConnectionPriority(priority: ConnectionPriority) {
        TODO("Not yet implemented")
    }

    override fun requestMtu(mtu: Int) {
        TODO("Not yet implemented")
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption) {
        TODO("Not yet implemented")
    }

    override fun readPhy() {
        TODO("Not yet implemented")
    }

    override fun readRssi() {
        TODO("Not yet implemented")
    }

    override fun disconnect() {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }
}