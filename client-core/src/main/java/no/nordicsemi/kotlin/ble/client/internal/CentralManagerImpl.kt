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

package no.nordicsemi.kotlin.ble.client.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.nordicsemi.kotlin.ble.client.CentralManager
import no.nordicsemi.kotlin.ble.client.Peripheral
import no.nordicsemi.kotlin.ble.client.ScanResult
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException

/**
 * Base implementation of [CentralManager].
 *
 * @param ID The type of the peripheral identifier.
 * @param P The type of the peripheral.
 * @param EX The type of the peripheral executor.
 * @param F The type of the scan filter scope.
 * @param SR Scan result type.
 * @property scope The coroutine scope.
 */
abstract class CentralManagerImpl<
    ID: Any,
    P: Peripheral<ID, EX>,
    EX: Peripheral.Executor<ID>,
    F: CentralManager.ScanFilterScope,
    SR: ScanResult<*, *>,
>(
    protected val scope: CoroutineScope,
): CentralManager<ID, P, EX, F, SR> {
    private var closeJob: Job
    private lateinit var internalScope: CoroutineScope

    init {
        // Make sure the Central Manager gets closed when its scope gets cancelled.
        // This coroutine gets cancelled when close() is called or when the scope gets cancelled.
        closeJob = scope.launch {
            internalScope = this
            try { awaitCancellation() }
            finally { withContext(NonCancellable) { close() } }
        }
    }

    /**
     * A list of peripherals managed by this Central Manager instance.
     */
    protected val managedPeripherals = mutableMapOf<ID, P>()

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
     * Returns the [Peripheral] object associated with given [id].
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
                        // Close the peripheral when the manager is closed.
                        newPeripheral.forceClose()
                    }
                    .onCompletion {
                        // Close the peripheral when the scope is cancelled.
                        newPeripheral.forceClose()
                    }
                    .launchIn(internalScope)
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

    // Implementation

    override fun close() {
        isOpen = false
        closeJob.cancel()
    }
}