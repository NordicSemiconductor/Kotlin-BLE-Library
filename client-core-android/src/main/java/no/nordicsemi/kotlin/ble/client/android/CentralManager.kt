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

package no.nordicsemi.kotlin.ble.client.android

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import no.nordicsemi.kotlin.ble.client.CentralManager
import no.nordicsemi.kotlin.ble.client.exception.ConnectionFailedException
import no.nordicsemi.kotlin.ble.core.exception.BluetoothUnavailableException
import no.nordicsemi.kotlin.ble.core.exception.ManagerClosedException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Android-specific interface of the Central Manager.
 *
 * This interface extends [CentralManager] and adds support for bonded devices,
 * connecting using Android-specific connection options, and getting peripherals by their
 * MAC addresses.
 */
interface CentralManager:
    CentralManager<String, Peripheral, Peripheral.Executor, ConjunctionFilterScope, ScanResult> {

    /**
     * Returns a list of peripherals for which the system has bond information.
     *
     * @return A list of bonded peripherals.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     */
    fun getBondedPeripherals(): List<Peripheral>

    /**
     * Establishes Bluetooth LE connection to the peripheral using default connection options.
     *
     * @param peripheral The peripheral to connect to.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     * @throws IllegalArgumentException If the Peripheral wasn't acquired from this manager
     * by scanning, ranging, or using methods [getPeripheralsById] or [getBondedPeripherals].
     * @throws ConnectionFailedException If connection failed. See [ConnectionFailedException.reason]
     * for a reason.
     * @throws CancellationException If the coroutine was canceled.
     * @throws TimeoutCancellationException If the connection attempt timed out.
     * @see [ConnectionOptions.Default]
     */
    override suspend fun connect(peripheral: Peripheral) {
        connect(peripheral, ConnectionOptions.Default)
    }

    /**
     * Establishes Bluetooth LE connection to the peripheral.
     *
     * This method does nothing if the device is already connected.
     *
     * @param peripheral The peripheral to connect to.
     * @param options Connection options.
     * @throws ManagerClosedException If the central manager has been closed.
     * @throws BluetoothUnavailableException If Bluetooth is disabled or not available.
     * @throws SecurityException If BLUETOOTH_CONNECT permission is denied.
     * @throws IllegalArgumentException If the Peripheral wasn't acquired from this manager
     * by scanning, [getPeripheralsById] or [getBondedPeripherals].
     * @throws ConnectionFailedException If connection failed. See [ConnectionFailedException.reason]
     * for a reason.
     * @throws CancellationException If the coroutine was canceled.
     * @throws TimeoutCancellationException If the connection attempt timed out.
     */
    suspend fun connect(peripheral: Peripheral, options: ConnectionOptions)

    // TODO Add scanning with scan mode, PHY and legacy settings

    // TODO Add scan with Pending Intent (Android 8+, or for compat for legacy?)

    companion object Factory
    
    /**
     * Android-specific connection options.
     *
     * @property automaticallyRequestHighestValueLength If true, the manager will automatically request
     * the highest MTU supported by the remote device immediately after establishing the connection.
     * @property opportunistic Opportunistic connection is available from Android 17 onwards.
     * An opportunistic GATT client does not hold a GATT connection. It automatically disconnects
     * when no other GATT connections are active for the remote device.
     */
    sealed class ConnectionOptions(
        open val automaticallyRequestHighestValueLength: Boolean,
        open val opportunistic: Boolean,
    ) {
        companion object {
            /**
             * Default connection options.
             */
            val Default: ConnectionOptions
                get() = Direct()
        }

        /**
         * Connection using Auto Connect feature.
         *
         * The option to passively scan and finalize the connection when the remote device is in
         * range and available. It does not allow to set a connection timeout and will not retry
         * on failure.
         *
         * In general, the first ever connection to a device should be direct and subsequent
         * connections to known devices should be invoked with this option.
         *
         * @param automaticallyRequestHighestValueLength If true, the manager will automatically request
         * the highest MTU supported by the remote device immediately after establishing the connection.
         * @param opportunistic Opportunistic connection is available from Android 17 onwards.
         * An opportunistic GATT client does not hold a GATT connection. It automatically disconnects
         * when no other GATT connections are active for the remote device.
         */
        data class AutoConnect(
            override val automaticallyRequestHighestValueLength: Boolean = true,
            override val opportunistic: Boolean = false,
        ): ConnectionOptions(automaticallyRequestHighestValueLength, opportunistic)

        /**
         * Connection options for direct connection.
         *
         * Direct connection has a maximum timeout which depends on the device manufacturer and is
         * usually 30 seconds or less. It may be shortened using the [timeout] parameter.
         *
         * @property timeout The connection timeout, defaults to 10 seconds.
         * If set to a longer time than the system timeout (around 30 seconds)
         * the connection will still timeout after the default timer. Use [AutoConnect] instead.
         * @property retry The number of connection retries. Value *N* indicates *N+1* connection attempts.
         * @property retryDelay The delay between connection retries, defaults to 300 ms.
         * @property automaticallyRequestHighestValueLength If true, the manager will automatically request
         * the highest MTU supported by the remote device immediately after establishing the connection.
         * @property opportunistic Opportunistic connection is available from Android 17 onwards.
         * An opportunistic GATT client does not hold a GATT connection. It automatically disconnects
         * when no other GATT connections are active for the remote device.
         */
        data class Direct(
            val timeout: Duration = 10.seconds,
            val retry: Int = 2,
            val retryDelay: Duration = 300.milliseconds,
            override val automaticallyRequestHighestValueLength: Boolean = true,
            override val opportunistic: Boolean = false
        ): ConnectionOptions(automaticallyRequestHighestValueLength, opportunistic)
    }
}