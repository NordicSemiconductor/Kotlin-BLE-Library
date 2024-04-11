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

package no.nordicsemi.kotlin.ble.client.android.preview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.kotlin.ble.client.android.ConnectionParametersChanged
import no.nordicsemi.kotlin.ble.client.android.ConnectionPriority
import no.nordicsemi.kotlin.ble.client.android.ConnectionStateChanged
import no.nordicsemi.kotlin.ble.client.android.GattEvent
import no.nordicsemi.kotlin.ble.client.android.MtuChanged
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.PeripheralType
import no.nordicsemi.kotlin.ble.client.android.PhyChanged
import no.nordicsemi.kotlin.ble.client.android.RssiRead
import no.nordicsemi.kotlin.ble.core.BondState
import no.nordicsemi.kotlin.ble.core.ConnectionParameters
import no.nordicsemi.kotlin.ble.core.ConnectionState
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PhyInUse
import no.nordicsemi.kotlin.ble.core.PhyOption

/**
 * A stub implementation of [Peripheral.Executor] for Android.
 *
 * It does not depend on any Android API and can be used to preview the UI in the Compose Preview.
 *
 * The stub implementation provides some mocking functionality, for example [connect]
 * immediately changes the connection state to [ConnectionState.Connected] and [disconnect]
 * to [ConnectionState.Disconnected], etc.
 *
 * @param address The MAC address of the peripheral.
 * @param name An optional name of the peripheral.
 * @param type The type of the peripheral, defaults to [PeripheralType.LE].
 * @param initialState The initial connection state of the peripheral.
 * @param rssi The signal strength of the peripheral in dBm.
 * @param hasBondInformation `true` if the Android device has the bond information for the peripheral,
 * that is, if the peripheral is bonded to the device.
 */
private class StubExecutor(
    override val address: String,
    override val name: String?,
    override val type: PeripheralType,
    override val initialState: ConnectionState,
    private val rssi: Int,
    private val phy: PhyInUse,
    hasBondInformation: Boolean,
): Peripheral.Executor {
    private val _events = MutableSharedFlow<GattEvent>(replay = 1)
    override val events: Flow<GattEvent> = _events.asSharedFlow()

    private val _bondState = MutableStateFlow(if (hasBondInformation) BondState.BONDED else BondState.NONE)
    override val bondState: StateFlow<BondState> = _bondState.asStateFlow()

    override fun connect(autoConnect: Boolean, preferredPhy: List<Phy>) {
        _events.tryEmit(ConnectionStateChanged(ConnectionState.Connected))
    }

    override fun requestConnectionPriority(priority: ConnectionPriority) {
        _events.tryEmit(ConnectionParametersChanged(ConnectionParameters.Connected(15, 0, 0)))
    }

    override fun requestMtu(mtu: Int) {
        _events.tryEmit(MtuChanged(mtu))
    }

    override fun requestPhy(txPhy: Phy, rxPhy: Phy, phyOptions: PhyOption) {
        _events.tryEmit(PhyChanged(PhyInUse(txPhy, rxPhy)))
    }

    override fun readPhy() {
        _events.tryEmit(PhyChanged(phy))
    }

    override fun readRssi() {
        _events.tryEmit(RssiRead(rssi))
    }

    override fun disconnect() {
        _events.tryEmit(ConnectionStateChanged(ConnectionState.Disconnected()))
    }

    override fun close() {
        // Do nothing
    }
}

/**
 * A preview implementation of [Peripheral] for Android.
 *
 * This class is used to preview the UI in the Compose Preview.
 *
 * @param scope The coroutine scope. This can be set to `rememberCoroutineScope()`.
 * @param address The MAC address of the peripheral.
 * @param name An optional name of the peripheral.
 * @param type The type of the peripheral, defaults to [PeripheralType.LE].
 * @param rssi The signal strength of the peripheral in dBm.
 * @param state The connection state of the peripheral.
 * @param hasBondInformation `true` if the Android device has the bond information for the peripheral,
 * that is, if the peripheral is bonded to the device. Defaults to `false`.
 */
open class PreviewPeripheral(
    scope: CoroutineScope,
    address: String = "00:11:22:33:44:55",
    name: String? = "My Device",
    type: PeripheralType = PeripheralType.LE,
    rssi: Int = -40, // dBm
    phy: PhyInUse = PhyInUse.LE_1M,
    state: ConnectionState = ConnectionState.Disconnected(),
    hasBondInformation: Boolean = false,
): Peripheral(
    scope = scope,
    impl = StubExecutor(address, name, type, state, rssi, phy, hasBondInformation)
) {
    override fun toString(): String {
        return name ?: address
    }
}