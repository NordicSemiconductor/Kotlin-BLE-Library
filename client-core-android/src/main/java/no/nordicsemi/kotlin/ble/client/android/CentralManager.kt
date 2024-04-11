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

import no.nordicsemi.kotlin.ble.client.GenericCentralManager
import no.nordicsemi.kotlin.ble.core.Phy
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Android-specific implementation of the Central Manager.
 *
 * This class implements the [GenericCentralManager] interface and adds support for bonded devices,
 * connecting using Android-specific connection options, and getting peripherals by their
 * MAC addresses.
 */
class CentralManager(
    private val engine: CentralManagerEngine<*>
): GenericCentralManager<String, Peripheral, ConjunctionFilterScope, ScanResult>(engine) {

    /**
     * Returns a list of peripherals for which the system has bond information.
     */
    fun getBondedPeripherals(): List<Peripheral> = engine.getBondedPeripherals()

    /**
     * Connects to the given device.
     *
     * @param peripheral The peripheral to connect to.
     * @param options Connection options.
     */
    suspend fun connect(peripheral: Peripheral, options: ConnectionOptions) =
        engine.connect(peripheral, options)

    companion object Factory
    
    /**
     * Android-specific connection options.
     */
    sealed class ConnectionOptions {
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
         * In general, the first ever connection to a device should be direct and subsequent connections
         * to known devices should be invoked with the this option.
         */
        data object AutoConnect: ConnectionOptions()

        /**
         * Connection options for direct connection.
         *
         * Direct connection has a maximum timeout which depends on the device manufacturer and is
         * usually 30 seconds or less. It may be shortened using the [timeout] parameter.
         *
         * @param timeout The connection timeout. 0 to default to system timeout.
         * Default timeout is 10 seconds.
         * @param retry The number of connection retries. Value *N* indicates *N+1* connection attempts.
         * @param retryDelay The delay between connection retries, defaults to 300 ms.
         * @param preferredPhy Preferred PHY for connections to remote LE device. Note that this is
         * just a recommendation, whether the PHY change will happen depends on other applications
         * preferences, local and remote controller capabilities. Controller can override these settings.
         */
        data class Direct(
            val timeout: Duration = 10.seconds,
            val retry: Int = 2,
            val retryDelay: Duration = 300.milliseconds,
            val preferredPhy: List<Phy> = listOf(Phy.PHY_LE_1M)
        ): ConnectionOptions() {

            /**
             * Connection options for direct connection.
             *
             * Direct connection has a maximum timeout which depends on the device manufacturer and is
             * usually 30 seconds or less. It may be shortened using the [timeout] parameter.
             *
             * @param timeout The connection timeout. 0 to default to system timeout.
             * Default timeout is 10 seconds.
             * @param retry The number of connection retries. Value *N* indicates *N+1* connection attempts.
             * @param retryDelay The delay between connection retries, defaults to 300 ms.
             * @param phy The preferred PHY for connections to remote LE device.
             */
            constructor(
                timeout: Duration = 10.seconds,
                retry: Int = 3,
                retryDelay: Duration = 300.milliseconds,
                vararg phy: Phy
            ): this(timeout, retry, retryDelay, preferredPhy = phy.toList())
        }
    }
}