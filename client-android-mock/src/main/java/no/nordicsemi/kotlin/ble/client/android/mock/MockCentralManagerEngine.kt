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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.CentralManagerEngine
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.GenericCentralManager
import no.nordicsemi.kotlin.ble.core.Manager
import kotlin.time.Duration


/**
 * Creates an implementation of the [CentralManager] which is using native Android API to
 * scan and connect to physical Bluetooth LE devices.
 *
 * @param scope The coroutine scope.
 * @property initialState The initial state of the Central Manager,
 * defaults to [Manager.State.POWERED_ON].
 * @property environment The environment to use for the mock, defaults to the latest supported API.
 */
fun CentralManager.Factory.mock(
    scope: CoroutineScope,
    initialState: Manager.State = Manager.State.POWERED_ON,
    environment: MockEnvironment = MockEnvironment.Api31()
) =
    CentralManager(MockCentralManagerEngine(scope, initialState, environment))

/**
 * A mock implementation of [GenericCentralManager] for Android.
 *
 * @param scope The coroutine scope.
 * @property initialState The initial state of the Central Manager,
 * defaults to [Manager.State.POWERED_ON].
 * @property environment The environment to use for the mock, defaults to the latest supported API.
 */
open class MockCentralManagerEngine(
    scope: CoroutineScope,
    private val initialState: Manager.State = Manager.State.POWERED_ON,
    private val environment: MockEnvironment = MockEnvironment.Api31(),
): CentralManagerEngine<Any>(scope, 1) {

    /**
     * Simulates a state change in the central manager.
     */
    fun simulateStateChange(newState: Manager.State) {
        ensureNotClosed()
        require(newState != Manager.State.UNKNOWN)
        _state.update { newState }
    }

    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<Manager.State>
        get() = _state.asStateFlow()

    override fun checkConnectPermission() {
        TODO("Not yet implemented")
    }

    override fun checkScanningPermission() {
        TODO("Not yet implemented")
    }

    override fun getPeripheralsById(ids: List<String>): List<Peripheral> {
        TODO("Not yet implemented")
    }

    override fun getBondedPeripherals(): List<Peripheral> {
        TODO("Not yet implemented")
    }

    override fun scan(
        timeout: Duration,
        filter: ConjunctionFilterScope.() -> Unit
    ): Flow<ScanResult> {
        TODO("Not yet implemented")
    }

    override fun monitor(
        timeout: Duration,
        filter: ConjunctionFilterScope.() -> Unit
    ): Flow<MonitoringEvent<Peripheral>> {
        TODO("Not yet implemented")
    }

    override fun range(peripheral: Peripheral, timeout: Duration): Flow<RangeEvent<Peripheral>> {
        TODO("Not yet implemented")
    }

    override suspend fun connect(
        peripheral: Peripheral,
        options: CentralManager.ConnectionOptions
    ) {
        TODO("Not yet implemented")
    }

    override fun close() {
        // Ignore if already closed.
        if (!isOpen)
            return

        // Set the state to unknown.
        _state.update { Manager.State.UNKNOWN }
        super.close()
    }

    private fun ensureNotClosed() {
        check(isOpen) { "CentralManager is closed" }
    }
}