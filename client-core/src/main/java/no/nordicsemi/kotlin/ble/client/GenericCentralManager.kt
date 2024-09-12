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

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.ble.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.client.exception.ScanningException
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.Peer
import java.io.Closeable
import kotlin.time.Duration

/**
 * A central manager is responsible for scanning, monitoring and connecting to Bluetooth LE devices.
 *
 * @param ID The type of the peripheral identifier.
 * @param P The type of the peripheral.
 * @param F The type of the scan filter scope.
 * @param SR Scan result type.
 *
 * @property engine The engine that performs the operations.
 */
abstract class GenericCentralManager<
        ID: Any,
        P: GenericPeripheral<ID, EX>,
        EX: GenericPeripheral.GenericExecutor<ID>,
        F: GenericCentralManager.ScanFilterScope,
        SR: GenericScanResult<*, *>,
>(
    private val engine: GenericCentralManagerEngine<ID, P, EX, F, SR>,
): Manager<GenericCentralManagerEngine<ID, P, EX, F, SR>>, Closeable {
    override val state: StateFlow<Manager.State> by lazy { engine.state }

    init {
        // Ensure the engine is not used by another manager.
        check(!engine.isUsed) { "The engine is already used by another manager." }
        engine.isUsed = true
    }

    /**
     * Returns a list of peripherals discovered by this instance of the Central Manager.
     *
     * @param ids List of peripheral identifiers.
     * @return List of peripherals. The list may have a smaller size than the input list.
     * @see [Peer.identifier]
     */
    fun getPeripheralsById(ids: List<ID>): List<P> = engine.getPeripheralsById(ids)

    /**
     * Returns a list of peripherals discovered by this instance of the Central Manager.
     *
     * @param id The peripheral identifier.
     * @return A peripheral associated with the given UUID, if found.
     * @see [Peer.identifier]
     */
    fun getPeripheralById(id: ID): P? = getPeripheralsById(listOf(id)).firstOrNull()

    /**
     * Scans for Bluetooth LE devices.
     *
     * The scan will be stopped after the given period of time or when the flow is cancelled.
     *
     * @param timeout The scan duration. By default the scan will run until the flow is closed.
     * @param filter The filter to apply. By default no filter is applied.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If the permission to scan is denied.
     * @throws ScanningException If an error occurred while starting the scan.
     */
    fun scan(timeout: Duration = Duration.INFINITE, filter: F.() -> Unit = {}): Flow<SR> =
        engine.scan(timeout, filter)

    /**
     * Starts monitoring for Bluetooth LE devices.
     *
     * This method will emit events when the monitoring device enters or leaves the range of a
     * peripheral advertising packets matching given filter.
     *
     * The scan will be stopped after the given period of time or when the flow is cancelled.
     *
     * @param timeout The scan duration. By default the scan will run until the flow is closed.
     * @param filter The filter to apply. By default no filter is applied.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If the permission to scan is denied.
     * @throws ScanningException If an error occurred while starting the scan.
     */
    fun monitor(timeout: Duration = Duration.INFINITE, filter: F.() -> Unit): Flow<MonitoringEvent<P>> =
        engine.monitor(timeout, filter)

    /**
     * Starts ranging for Bluetooth LE devices.
     *
     * This method can be started when [monitor] reports [PeripheralEnteredRange] event for given
     * peripheral.
     *
     * The flow is closed automatically when the peripheral leaves range of the monitoring device.
     * @param peripheral The peripheral to range.
     * @param timeout The scan duration. By default the scan will run until the flow is closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If the permission to scan is denied.
     * @throws ScanningException If an error occurred while starting the scan.
     */
    fun range(peripheral: P, timeout: Duration = Duration.INFINITE): Flow<RangeEvent<P>> =
        engine.range(peripheral, timeout)

    /**
     * Connects to the given device.
     *
     * @param peripheral The peripheral to connect to.
     */
    suspend fun connect(peripheral: P) = engine.connect(peripheral)

    /**
     * Closes the manager and releases its resources.
     *
     * This method must be called when the manager is no longer needed.
     */
    override fun close() = engine.close()

    /**
     * A base interface for scan filter scope.
     *
     * Different platforms may allow to filter devices based on different criteria.
     */
    interface ScanFilterScope
}