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

package no.nordicsemi.kotlin.ble.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.client.exception.ScanningException
import no.nordicsemi.kotlin.ble.core.Engine
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.Peer
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException
import kotlin.time.Duration

/**
 * An engine is responsible for scanning, monitoring and connecting to Bluetooth LE devices.
 *
 * @param ID The type of the peripheral identifier.
 * @param P The type of the peripheral.
 * @param F The type of the scan filter scope.
 * @param SR Scan result type.
 * @property scope The coroutine scope.
 */
abstract class GenericCentralManagerEngine<
        ID: Any,
        P: GenericPeripheral<ID, EX>,
        EX: GenericPeripheral.GenericExecutor<ID>,
        F: GenericCentralManager.ScanFilterScope,
        SR: GenericScanResult<*, *>,
>(
    protected val scope: CoroutineScope,
): Engine {
    /**
     * A flag indicating whether the engine is used by a manager.
     */
    private var isUsed: Boolean = false

    /**
     * Claims the engine for a manager.
     */
    internal fun claim() {
        if (isUsed) {
            throw IllegalStateException("The engine is already in use.")
        }
        isUsed = true
    }

    /**
     * A list of peripherals managed by this Central Manager instance.
     */
    private val managedPeripherals = mutableMapOf<ID, P>()

    /**
     * Checks whether the given peripheral was obtained using this instance
     * of the Central Manager.
     */
    protected fun checkPeripheral(peripheral: P) {
        require(managedPeripherals.containsValue(peripheral)) {
            "$peripheral was not obtained using this Central Manager instance"
        }
    }

    /**
     * Returns the [GenericPeripheral] object associated with given [id].
     *
     * If the Central Manager engine does not have a matching peripheral, the factory method
     * is called to create it.
     *
     * @param id The peripheral ID.
     * @param factory A lambda that should return a new peripheral instance for the given ID.
     * @return The peripheral.
     */
    protected fun peripheral(id: ID, factory: (ID) -> P): P {
        return managedPeripherals.getOrPut(id) {
            factory(id).also { newPeripheral ->
                // Make sure the new peripheral is closed when the manager gets closed or
                // the scope gets cancelled.
                state
                    .filter { it != Manager.State.POWERED_ON }
                    .onEach {
                        newPeripheral.forceClose()
                    }
                    .onCompletion {
                        newPeripheral.forceClose()
                    }
                    .launchIn(scope)
            }
        }
    }

    /**
     * Flag indicating if the central manager is open.
     *
     * This is set to false when [close] is called.
     */
    protected var isOpen = true
        private set

    /**
     * Checks if the central manager is open, otherwise throws [ManagerClosedException].
     */
    protected fun ensureOpen() {
        require(isOpen) { throw ManagerClosedException() }
    }

    /**
     * Closes the manager engine.
     *
     * The engine cannot be used after it has been closed.
     */
    protected open fun close() {
        isOpen = false
    }

    /**
     * Closes the manager engine.
     *
     * This method just calls [close], but has `internal` access modifier to allow calling it
     * from the manager.
     */
    internal fun finalize() {
        close()
    }

    /**
     * Returns a list of peripherals with given IDs, known by this instance of the Central Manager.
     *
     * @param ids List of peripheral identifiers.
     * @return List of peripherals. The list may have a smaller size than the input list.
     * @see [Peer.identifier]
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     */
    abstract fun getPeripheralsById(ids: List<ID>): List<P>

    /**
     * Scans for Bluetooth LE devices.
     *
     * The scan will be stopped after the given period of time or when the flow is cancelled.
     *
     * @param timeout The scan duration. By default the scan will run until the flow is closed.
     * @param filter The filter to apply. By default no filter is applied.
     * @return A flow of scan results.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If the permission to scan is denied.
     * @throws ScanningException If an error occurred while starting the scan.
     */
    abstract fun scan(timeout: Duration = Duration.INFINITE, filter: F.() -> Unit = {}): Flow<SR>

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
     * @return A flow of monitoring events.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If the permission to scan is denied.
     * @throws ScanningException If an error occurred while starting the scan.
     */
    abstract fun monitor(timeout: Duration = Duration.INFINITE, filter: F.() -> Unit): Flow<MonitoringEvent<P>>

    /**
     * Starts ranging for Bluetooth LE devices.
     *
     * This method can be started when [monitor] reports [PeripheralEnteredRange] event for given
     * peripheral.
     *
     * The flow is closed automatically when the peripheral leaves range of the monitoring device.
     * @param peripheral The peripheral to range.
     * @param timeout The scan duration. By default the scan will run until the flow is closed.
     * @return A flow of ranging events.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If the permission to scan is denied.
     * @throws ScanningException If an error occurred while starting the scan.
     */
    abstract fun range(peripheral: P, timeout: Duration = Duration.INFINITE): Flow<RangeEvent<P>>

    /**
     * Connects to the given device.
     *
     * @param peripheral The peripheral to connect to.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws IllegalArgumentException If the Peripheral wasn't acquired from this manager
     * by scanning or [getPeripheralsById].
     */
    abstract suspend fun connect(peripheral: P)
}