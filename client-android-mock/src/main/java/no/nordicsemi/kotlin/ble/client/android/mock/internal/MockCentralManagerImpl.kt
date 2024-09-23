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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import no.nordicsemi.kotlin.ble.android.mock.LatestApi
import no.nordicsemi.kotlin.ble.android.mock.MockEnvironment
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.android.internal.CentralManagerImpl
import no.nordicsemi.kotlin.ble.client.android.mock.MockCentralManager
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpec
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException
import org.slf4j.LoggerFactory
import kotlin.time.Duration

/**
 * A mock implementation of [CentralManager] for Android.
 *
 * @param scope The coroutine scope.
 * @property environment The environment to use for the mock, defaults to the latest supported API.
 */
open class MockCentralManagerImpl(
    scope: CoroutineScope,
    private val environment: MockEnvironment = LatestApi(),
): MockCentralManager, CentralManagerImpl<Unit>(scope, Unit) {
    private val logger = LoggerFactory.getLogger(MockCentralManagerImpl::class.java)

    // Simulation methods

    override fun simulatePowerOn() = simulateStateChange(Manager.State.POWERED_ON)

    override fun simulatePowerOff() = simulateStateChange(Manager.State.POWERED_OFF)

    override fun simulatePeripherals(peripherals: List<PeripheralSpec<String>>) {

    }

    override fun tearDownSimulation() {
        TODO("Not yet implemented")
    }

    /**
     * Simulates a state change in the central manager.
     *
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is not supported on the device.
     */
    private fun simulateStateChange(newState: Manager.State) {
        require(environment.isBluetoothSupported) {
            throw BluetoothUnavailableException()
        }

        ensureOpen()

        // Ignore if the state has not changed.
        if (newState == state.value)
            return

        logger.info("Bluetooth state changed: ${state.value} -> $newState")
        _state.update { newState }
    }

    // Implementation

    private val _state = MutableStateFlow(
        when {
            !environment.isBluetoothSupported -> Manager.State.UNSUPPORTED
            !environment.isBluetoothEnabled -> Manager.State.POWERED_OFF
            else -> Manager.State.POWERED_ON
        }
    )
    override val state: StateFlow<Manager.State>
        get() = _state.asStateFlow()

    override fun checkConnectPermission() {
        check(!environment.requiresBluetoothRuntimePermissions || environment.isBluetoothConnectPermissionGranted) {
            throw SecurityException("BLUETOOTH_CONNECT permission not granted")
        }
    }

    override fun checkScanningPermission() {
        check(!environment.requiresBluetoothRuntimePermissions || environment.isBluetoothScanPermissionGranted) {
            throw SecurityException("BLUETOOTH_SCAN permission not granted")
        }
    }

    override fun getPeripheralsById(ids: List<String>): List<Peripheral> {
        // Ensure the central manager has not been closed.
        ensureOpen()

        TODO("Not yet implemented")
//        return ids.map { id ->
//            peripheral(id) {
//                Peripheral(
//                    scope = scope,
//                    impl = MockExecutor(
//                        address = id,
//                    )
//                )
//            }
//        }
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

    override fun close() {
        // Ignore if already closed.
        if (!isOpen)
            return

        // Set the state to unknown.
        _state.update { Manager.State.UNKNOWN }
        super.close()
    }
}