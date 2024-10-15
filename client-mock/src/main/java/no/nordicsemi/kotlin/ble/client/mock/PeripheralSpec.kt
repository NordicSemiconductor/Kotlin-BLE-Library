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

package no.nordicsemi.kotlin.ble.client.mock

import no.nordicsemi.kotlin.ble.core.AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.LegacyAdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.ServerScope
import no.nordicsemi.kotlin.ble.core.internal.ServerScopeImpl
import no.nordicsemi.kotlin.ble.core.internal.ServiceDefinition
import no.nordicsemi.kotlin.ble.core.mock.AdvertisingDataScope
import no.nordicsemi.kotlin.ble.core.mock.internal.AdvertisingDataScopeImpl
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class PeripheralSpec<ID> private constructor(
    private val identifier: ID,
    private val proximity: Proximity,
    private val advertisement: List<MockAdvertisementConfig>?,
) {

    companion object {

        /**
         * Simulates a peripheral with given ID and proximity.
         *
         * @param identifier The peripheral identifier.
         * @param proximity Approximate distance to the peripheral.
         * By default set to [Proximity.IMMEDIATE].
         */
        fun <ID> simulatePeripheral(
            identifier: ID,
            proximity: Proximity = Proximity.IMMEDIATE,
            builder: Builder<ID>.() -> Unit = {}
        ): PeripheralSpec<ID> {
            return Builder(identifier, proximity).apply(builder).build()
        }
    }

    fun simulateProximityChange(proximity: Proximity) {

    }

    /**
     * The builder for the [PeripheralSpec].
     *
     * @param ID The type of the peripheral identifier.
     * @param identifier The peripheral identifier.
     * @param proximity Approximate distance to the peripheral.
     */
    class Builder<ID> internal constructor(
        private val identifier: ID,
        private val proximity: Proximity,
    ) {
        private var advertisements: List<MockAdvertisementConfig>? = null
        private var name: String? = null
        private var services: List<ServiceDefinition>? = null
        private var eventHandler: PeripheralSpecEventHandler? = null
        private var connectionInterval: Duration? = null
        private var mtu: Int? = null
        private var supportedPhy: List<Phy>? = null
        private var isInitialConnection: Boolean = false
        private var isKnown: Boolean = false

        fun advertising(
            parameters: AdvertisingSetParameters = LegacyAdvertisingSetParameters(true),
            isAdvertisingWhenConnected: Boolean = false,
            builder: AdvertisingDataScope.() -> Unit,
        ): Builder<ID> = apply {
            val advertisingData = AdvertisingDataScopeImpl().apply(builder).build()
            val advertisement = MockAdvertisementConfig(parameters, isAdvertisingWhenConnected, advertisingData)
            advertisements = advertisements?.plus(advertisement) ?: listOf(advertisement)
        }

        /**
         * Makes the device connectable, but not connected at the moment of initialization.
         *
         * @param name The name of the peripheral, available from Device Name characteristic.
         * @param services The services available after service discovery.
         * @param eventHandler The event handler that will be called for every event emulating
         * a real peripheral.
         * @param connectionInterval The connection interval when connected.
         * @param mtu The maximum supported MTU size.
         */
        fun connectable(
            name: String,
            services: ServerScope.() -> Unit,
            eventHandler: PeripheralSpecEventHandler,
            connectionInterval: Duration = 45.milliseconds,
            mtu: Int = 23,
        ): Builder<ID> = apply {
            this.name = name
            this.services = ServerScopeImpl().apply(services).build()
            this.eventHandler = eventHandler
            this.connectionInterval = connectionInterval
            this.mtu = mtu
            this.isInitialConnection = false
        }

        /**
         * Makes the device connectable and already connected to the Android device at the moment
         * of initialization.
         *
         * @param name The name of the peripheral, available from Device Name characteristic.
         * @param services The services available after service discovery.
         * @param eventHandler The event handler that will be called for every event emulating
         * a real peripheral.
         * @param connectionInterval The connection interval when connected.
         * @param mtu The maximum supported MTU size.
         */
        fun connected(
            name: String,
            services: ServerScope.() -> Unit,
            eventHandler: PeripheralSpecEventHandler,
            connectionInterval: Duration = 45.milliseconds,
            mtu: Int = 23,
        ): Builder<ID> = apply {

        }

        /**
         * Makes a connection to the device possible without scanning.
         *
         * If this method is not called, the device needs to be scanned before connection attempt.
         */
        fun allowForRetrieval(): Builder<ID> = apply {
            isKnown = true
        }

        /**
         * Builds the [PeripheralSpec] object.
         */
        fun build(): PeripheralSpec<ID> = PeripheralSpec(identifier, proximity, advertisements)
    }
}