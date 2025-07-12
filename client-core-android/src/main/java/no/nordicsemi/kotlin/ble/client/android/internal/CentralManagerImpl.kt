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

package no.nordicsemi.kotlin.ble.client.android.internal

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.kotlin.ble.client.android.CentralManager
import no.nordicsemi.kotlin.ble.client.internal.CentralManagerImpl
import no.nordicsemi.kotlin.ble.client.android.CentralManager.ConnectionOptions
import no.nordicsemi.kotlin.ble.client.android.ConjunctionFilterScope
import no.nordicsemi.kotlin.ble.client.android.Peripheral
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.ble.client.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException

/**
 * Android-specific implementation of a central manager interface.
 *
 * @param C The context type.
 * @param scope The coroutine scope.
 */
abstract class CentralManagerImpl<C: Any>(
    scope: CoroutineScope,
): CentralManagerImpl<String, Peripheral, Peripheral.Executor, ConjunctionFilterScope, ScanResult>(scope),
    CentralManager {

    /**
     * Checks whether the BLUETOOTH_CONNECT permission is granted.
     *
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    protected abstract fun checkConnectPermission()

    /**
     * Checks whether the BLUETOOTH_SCAN permission is granted.
     *
     * @throws SecurityException If BLUETOOTH_SCAN permission is denied.
     */
    protected abstract fun checkScanningPermission()

    /**
     * Connects to the given device.
     *
     * @param peripheral The peripheral to connect to.
     * @param options Connection options.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     * @throws IllegalArgumentException If the Peripheral wasn't acquired from this manager
     * by scanning, [getPeripheralsById] or [getBondedPeripherals].
     */
    override suspend fun connect(
        peripheral: Peripheral,
        options: ConnectionOptions
    ) {
        // Ensure the central manager has not been closed.
        ensureOpen()

        // Ensure the peripheral was acquired from this Central Manager.
        checkPeripheral(peripheral)

        // Ensure Bluetooth is enabled.
        check(state.value == Manager.State.POWERED_ON) {
            throw BluetoothUnavailableException()
        }

        // Verify the BLUETOOTH_CONNECT permission is granted (Android 12+).
        checkConnectPermission()

        // Finally, connect to the peripheral.
        peripheral.connect(options)
    }

}