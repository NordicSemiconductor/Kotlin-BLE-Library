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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import no.nordicsemi.kotlin.ble.android.mock.LatestApi
import no.nordicsemi.kotlin.ble.android.mock.MockEnvironment
import no.nordicsemi.kotlin.ble.client.MonitoringEvent
import no.nordicsemi.kotlin.ble.client.RangeEvent
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.android.exception.ScanningFailedToStartException
import no.nordicsemi.kotlin.ble.client.android.internal.CentralManagerImpl
import no.nordicsemi.kotlin.ble.client.android.internal.ConjunctionFilter
import no.nordicsemi.kotlin.ble.client.android.internal.match
import no.nordicsemi.kotlin.ble.client.android.mock.MockCentralManager
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.client.mock.PeripheralSpec
import no.nordicsemi.kotlin.ble.client.mock.internal.MockBluetoothLeAdvertiser
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.Manager.State.UNKNOWN
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
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
): MockCentralManager, CentralManagerImpl(scope) {
    private val logger = LoggerFactory.getLogger(MockCentralManagerImpl::class.java)

    // Simulation methods
    private var peripheralSpecs = mutableListOf<PeripheralSpec<String>>()
    private val mockAdvertiser = MockBluetoothLeAdvertiser<String>(scope, environment)

    override fun simulatePowerOn() = simulateStateChange(Manager.State.POWERED_ON)

    override fun simulatePowerOff() = simulateStateChange(Manager.State.POWERED_OFF)

    override fun simulatePeripherals(peripherals: List<PeripheralSpec<String>>) {
        require(peripheralSpecs.isEmpty()) {
            "Peripherals have already been added to the simulation"
        }
        peripherals.forEach {
            // Validate the MAC address.
            require(it.identifier.matches(Regex("([0-9A-Fa-f]{2}:){5}([0-9A-Fa-f]{2})"))) {
                "Invalid MAC address: ${it.identifier}"
            }
        }
        peripheralSpecs.addAll(peripherals)
        managedPeripherals
        mockAdvertiser.simulateAdvertising(peripherals)
    }

    override fun tearDownSimulation() {
        mockAdvertiser.cancel()
        peripheralSpecs.clear()
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
    private val _advertisingEvents = MutableSharedFlow<ScanResult>()

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

        return peripheralSpecs
            .filter { it.identifier in ids }
            .map { peripheralSpec ->
                peripheral(peripheralSpec.identifier) {
                    Peripheral(
                        scope = scope,
                        impl = MockExecutor(peripheralSpec, null)
                    )
                }
            }
    }

    override fun getBondedPeripherals(): List<Peripheral> {
        // Ensure the central manager has not been closed.
        ensureOpen()

        // Verify the BLUETOOTH_CONNECT permission is granted (Android 12+).
        checkConnectPermission()

        // Here we need to concatenate two lists:
        // - known peripherals with bond information
        // - peripherals that have not been scanned yet, but are bonded (PeripheralSpec.isBonded == true)
        // Should we just iterate over peripheral specs, we would miss those that were bonded
        // in in runtime and would make all specs "known" (available for retrieval).
        // Should we iterate only managed, we would miss devices that were defined as bonded,
        // but were not scanned yet.
        val managedBondedPeripherals = managedPeripherals.values
            .filter { it.hasBondInformation }
        val otherBondedPeripherals = peripheralSpecs
            .filter { it.isBonded }
            .filter { it.identifier !in managedPeripherals.keys }
            .map { peripheralSpec ->
                peripheral(peripheralSpec.identifier) {
                    Peripheral(
                        scope = scope,
                        impl = MockExecutor(peripheralSpec, peripheralSpec.name)
                    )
                }
            }
        // TODO any order?
        return managedBondedPeripherals + otherBondedPeripherals
    }

    override fun scan(
        timeout: Duration,
        filter: ConjunctionFilterScope.() -> Unit
    ): Flow<ScanResult> {
        // Ensure the central manager has not been closed.
        ensureOpen()

        // Ensure Bluetooth is supported.
        check(environment.isBluetoothSupported && environment.isBluetoothEnabled) {
            throw BluetoothUnavailableException()
        }

        // Verify the BLUETOOTH_SCAN permission is granted (Android 12+).
        checkScanningPermission()

        // Build the filter based on the provided builder
        val filters = ConjunctionFilter().apply(filter).filters
        filters?.let {
            logger.trace("Starting scanning with filters: {}", it)
        } ?: logger.trace("Starting scanning with no filters")

        return flow {
            val reportResult = environment.scanner().getOrThrow()

            // Emit all scan results until the timeout.
            withTimeoutOrNull<Nothing>(timeout) {
                mockAdvertiser.events.collect { result ->
                    // Some (most?) Android devices do not report scan error using `onScanFailed`
                    // callback, but instead don't return any results.
                    // Re
                    if (!reportResult) {
                        return@collect
                    }

                    // Starting from Android 6 Location permission and Location service are required
                    // to scan for BLE devices. Since Android 12, apps can set a `neverForLocation`
                    // flag to claim that they won't estimate user's location from scan results.
                    if (environment.isLocationRequiredForScanning &&
                        (!environment.isLocationPermissionGranted || !environment.isLocationEnabled)) {
                        return@collect
                    }

                    // If the `neverForLocation` flag is set, check if the device is a beacon.
                    if (!environment.isLocationRequiredForScanning &&
                        environment.androidSdkVersion >= MockEnvironment.AndroidSdkVersion.S &&
                        result.isBeacon) {
                        return@collect
                    }

                    // If PHY LE Coded is not supported, ignore results sent with LE Coded PHY.
                    if (result.primaryPhy == PrimaryPhy.PHY_LE_CODED &&
                        (!environment.isLeCodedPhySupported || !environment.isScanningOnLeCodedPhySupported)
                    ) {
                        return@collect
                    }
                    if (result.secondaryPhy == Phy.PHY_LE_CODED && !environment.isLeCodedPhySupported) {
                        return@collect
                    }

                    // If PHY LE 2M is not supported, ignore results sent with LE 2M PHY as secondary PHY.
                    if (result.secondaryPhy == Phy.PHY_LE_2M && !environment.isLe2MPhySupported) {
                        return@collect
                    }

                    // TODO We're assuming that we scan with "ALL_SUPPORTED" PHYs, as this is not configurable in Native scanner yet.

                    // The mock scanner found the device and cached its MAC address.
                    result.peripheralSpec.simulateCaching()

                    val scanResult = result.toScanResult { peripheralSpec, name ->
                        peripheral(peripheralSpec.identifier) {
                            Peripheral(
                                scope = scope,
                                impl = MockExecutor(peripheralSpec, name)
                            )
                        }
                    }

                    // Apply the filters if set.
                    if (filters?.match(scanResult) == false) return@collect

                    emit(scanResult)
                }
            }
            logger.trace("Scanning timed out after {}", timeout)
        }.catch { throwable ->
            (throwable as? ScanningFailedToStartException)?.let {
                logger.error(it.message)
            }
        }.onCompletion {
            logger.trace("Scanning stopped")
        }
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
        if (!isOpen) return
        super.close()

        // Set the state to unknown.
        _state.update { UNKNOWN }
    }
}