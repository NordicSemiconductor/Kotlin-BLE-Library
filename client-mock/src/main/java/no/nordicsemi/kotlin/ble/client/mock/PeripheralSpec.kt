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
import no.nordicsemi.kotlin.ble.core.Bluetooth5AdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.LegacyAdvertisingSetParameters
import no.nordicsemi.kotlin.ble.core.PeripheralType
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import no.nordicsemi.kotlin.ble.core.ServerScope
import no.nordicsemi.kotlin.ble.core.internal.ServerScopeImpl
import no.nordicsemi.kotlin.ble.core.internal.ServiceDefinition
import no.nordicsemi.kotlin.ble.core.mock.AdvertisingDataScope
import no.nordicsemi.kotlin.ble.core.mock.internal.AdvertisingDataScopeImpl
import org.jetbrains.annotations.Range
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * A specification of a peripheral that can be simulated.
 *
 * Use [PeripheralSpec.simulatePeripheral] to create a new peripheral specification.
 *
 * @property identifier The peripheral identifier.
 * @property type The peripheral type.
 * @property proximity The proximity of the peripheral.
 * @property advertisement List of advertisement configurations.
 * @property name The name of the peripheral, exposed with Device Name characteristic.
 * @property services The services available after service discovery.
 * @property eventHandler The event handler that will be called for every event emulating
 * a real peripheral.
 * @property connectionInterval The connection interval when connected.
 * @property maxMtu The maximum supported MTU size.
 * @property supportedPhy The set of supported PHYs.
 * @property isConnected Whether the peripheral is connected.
 * @property isKnown Whether the peripheral has been scanned before and its address type is cached.
 * It is not possible to connect to an unknown device without scanning.
 * @property isBonded Whether the peripheral is bonded.
 */
class PeripheralSpec<ID> private constructor(
    identifier: ID,
    val type: PeripheralType,
    initialProximity: Proximity,
    val advertisement: List<MockAdvertisementConfig>?,
    val name: String?,
    val services: List<ServiceDefinition>?,
    val eventHandler: PeripheralSpecEventHandler?,
    val connectionInterval: Duration?,
    val maxMtu: Int?,
    val supportedPhy: Set<Phy>,
    isInitiallyConnected: Boolean,
    isKnown: Boolean,
    isBonded: Boolean,
) {
    var identifier: ID = identifier
        private set

    var proximity: Proximity = initialProximity
        private set

    var isConnected: Boolean = isInitiallyConnected
        private set

    var isKnown: Boolean = isKnown
        private set

    var isBonded: Boolean = isBonded
        private set

    companion object {

        /**
         * Simulates a peripheral with given ID and proximity.
         *
         * @param identifier The peripheral identifier.
         * @param type The peripheral type. By default set to [PeripheralType.LE].
         * @param proximity Approximate distance to the peripheral.
         * By default set to [Proximity.IMMEDIATE].
         */
        fun <ID> simulatePeripheral(
            identifier: ID,
            type: PeripheralType = PeripheralType.LE,
            proximity: Proximity = Proximity.IMMEDIATE,
            builder: Builder<ID>.() -> Unit = {}
        ): PeripheralSpec<ID> {
            return Builder(identifier, type, proximity).apply(builder).build()
        }
    }

    /**
     * Simulates a change in the proximity of the peripheral.
     *
     * @param proximity The new proximity. Use [Proximity.OUT_OF_RANGE] to simulate leaving the range.
     */
    fun simulateProximityChange(proximity: Proximity) {
        this.proximity = proximity

        if (proximity == Proximity.OUT_OF_RANGE) {
            isConnected
        }
    }

    /**
     * Simulates the situation when the device is scanned and advertisement data is received.
     *
     * If the device is not known, the device will be marked as known and will be allowed
     * for connection.
     */
    fun simulateCaching() {
        isKnown = true
    }

    /**
     * Simulates the situation when another application on the device
     * connects to the device.
     *
     * If the device has advertisement configuration with
     * [MockAdvertisementConfig.isAdvertisingWhenConnected] flag set
     * to `false`, the given advertisement will stop.
     *
     * A manager registered for connection event will receive an event.
     */
    fun simulateConnection() {
        TODO("Not yet implemented")
    }

    /**
     * Simulates peripheral disconnection from the device.
     *
     * The central manager will receive a disconnection event.
     *
     * @param reason The reason for disconnection.
     */
    fun simulateDisconnection(reason: DisconnectionReason) {
        TODO("Not yet implemented")
    }

    fun simulateMacChange(newIdentifier: ID) {
        identifier = newIdentifier
        isKnown = false
    }

    fun simulateReset() {
        eventHandler?.onReset()
        simulateDisconnection(Timeout)
    }

    // TODO Add more simulation methods

    /**
     * The builder for the [PeripheralSpec].
     *
     * @param ID The type of the peripheral identifier.
     * @param identifier The peripheral identifier.
     * @param proximity Approximate distance to the peripheral.
     */
    class Builder<ID> internal constructor(
        private val identifier: ID,
        private val type: PeripheralType,
        private val proximity: Proximity,
    ) {
        private var advertisements: List<MockAdvertisementConfig>? = null
        private var name: String? = null
        private var services: List<ServiceDefinition>? = null
        private var eventHandler: PeripheralSpecEventHandler? = null
        private var connectionInterval: Duration? = null
        private var maxMtu: Int? = null
        private var supportedPhy: MutableSet<Phy> = mutableSetOf(Phy.PHY_LE_1M)
        private var isInitiallyConnected: Boolean = false
        private var isKnown: Boolean = false
        private var isBonded: Boolean = false

        /**
         * Adds an advertising set to the peripheral.
         *
         * @param parameters The advertising parameters.
         * @param delay The delay before the advertising starts.
         * @param timeout The advertising timeout, since the start of advertising. By default set to infinite.
         * @param isAdvertisingWhenConnected Whether the device should advertise when connected.
         * @param advertisingData The builder for the advertising data.
         */
        fun advertising(
            parameters: AdvertisingSetParameters = LegacyAdvertisingSetParameters(true),
            delay: Duration = Duration.ZERO,
            timeout: Duration = Duration.INFINITE,
            isAdvertisingWhenConnected: Boolean = false,
            advertisingData: AdvertisingDataScope.() -> Unit,
        ): Builder<ID> = addAdvertisingSet(
            parameters = parameters,
            delay = delay,
            timeout = timeout,
            maxAdvertisingEvents = Int.MAX_VALUE,
            isAdvertisingWhenConnected = isAdvertisingWhenConnected,
            builder = advertisingData
        )

        /**
         * Adds an advertising set to the peripheral.
         *
         * @param parameters The advertising parameters.
         * @param delay The delay before the advertising starts.
         * @param maxAdvertisingEvents The maximum number of advertising events, in range 1..255.
         * @param isAdvertisingWhenConnected Whether the device should advertise when connected.
         * @param advertisingData The builder for the advertising data.
         */
        fun advertising(
            parameters: AdvertisingSetParameters = LegacyAdvertisingSetParameters(true),
            delay: Duration = Duration.ZERO,
            maxAdvertisingEvents: @Range(from = 1L, to = 255L) Int,
            isAdvertisingWhenConnected: Boolean = false,
            advertisingData: AdvertisingDataScope.() -> Unit,
        ): Builder<ID> = addAdvertisingSet(
            parameters = parameters,
            delay = delay,
            timeout = Duration.INFINITE,
            maxAdvertisingEvents = maxAdvertisingEvents,
            isAdvertisingWhenConnected = isAdvertisingWhenConnected,
            builder = advertisingData
        )

        /**
         * Makes the device connectable, but not connected at the moment of initialization.
         *
         * @param name The name of the peripheral, available from Device Name characteristic.
         * @param connectionInterval The connection interval when connected.
         * @param maxMtu The maximum supported MTU size.
         * @param isPhyLe2MSupported Whether the device supports PHY LE 2M.
         * @param isPhyCodedSupported Whether the device supports PHY LE Coded.
         * @param eventHandler The event handler that will be called for every event emulating
         * a real peripheral.
         * @param services The services available after service discovery.
         */
        fun connectable(
            name: String,
            connectionInterval: Duration = 45.milliseconds,
            maxMtu: Int = 23,
            isPhyLe2MSupported: Boolean = false,
            isPhyCodedSupported: Boolean = false,
            eventHandler: PeripheralSpecEventHandler,
            services: ServerScope.() -> Unit,
        ): Builder<ID> = setConnectionParameters(
            name = name,
            connectionInterval = connectionInterval,
            maxMtu = maxMtu,
            isPhyLe2MSupported = isPhyLe2MSupported,
            isPhyCodedSupported = isPhyCodedSupported,
            eventHandler = eventHandler,
            services = services
        ).also {
            isInitiallyConnected = false
        }

        /**
         * Makes the device connectable and already connected to the device at the moment
         * of initialization (if it's in range).
         *
         * Connected devices are also allowed for retrieval.
         *
         * @param name The name of the peripheral, available from Device Name characteristic.
         * @param connectionInterval The connection interval when connected.
         * @param maxMtu The maximum supported MTU size.
         * @param isPhyLe2MSupported Whether the device supports PHY LE 2M.
         * @param isPhyCodedSupported Whether the device supports PHY LE Coded.
         * @param eventHandler The event handler that will be called for every event emulating
         * a real peripheral.
         * @param services The services available after service discovery.
         */
        fun connected(
            name: String,
            connectionInterval: Duration = 45.milliseconds,
            maxMtu: Int = 23,
            isPhyLe2MSupported: Boolean = false,
            isPhyCodedSupported: Boolean = false,
            eventHandler: PeripheralSpecEventHandler,
            services: ServerScope.() -> Unit,
        ): Builder<ID> = setConnectionParameters(
            name = name,
            connectionInterval = connectionInterval,
            maxMtu = maxMtu,
            isPhyLe2MSupported = isPhyLe2MSupported,
            isPhyCodedSupported = isPhyCodedSupported,
            eventHandler = eventHandler,
            services = services
        ).also {
            isInitiallyConnected = proximity != Proximity.OUT_OF_RANGE
        }

        /**
         * Makes a connection to the device possible without scanning.
         *
         * If this method is not called, the device needs to be scanned before connection attempt.
         *
         * Bonded devices are automatically allowed for retrieval.
         * @see bonded
         */
        fun allowForRetrieval(): Builder<ID> = apply {
            isKnown = true
        }

        /**
         * Makes the device bonded.
         *
         * If this method is not called, the device will not be bonded.
         *
         * Bonded devices are also automatically allowed for retrieval.
         * @see allowForRetrieval
         */
        fun bonded(): Builder<ID> = apply {
            isBonded = true
            isKnown = true
        }

        /**
         * Builds the [PeripheralSpec] object.
         */
        fun build(): PeripheralSpec<ID> = PeripheralSpec(
            identifier = identifier,
            type = type,
            initialProximity = proximity,
            advertisement = advertisements,
            name = name,
            services = services,
            eventHandler = eventHandler,
            connectionInterval = connectionInterval,
            maxMtu = maxMtu,
            supportedPhy = supportedPhy,
            isInitiallyConnected = isInitiallyConnected,
            isKnown = isKnown,
            isBonded = isBonded,
        )

        // Implementation

        private fun addAdvertisingSet(
            parameters: AdvertisingSetParameters,
            delay: Duration,
            timeout: Duration,
            maxAdvertisingEvents: Int,
            isAdvertisingWhenConnected: Boolean = false,
            builder: AdvertisingDataScope.() -> Unit,
        ): Builder<ID> = apply {
            val advertisingData =
                AdvertisingDataScopeImpl(parameters.txPowerLevel).apply(builder).build()
            val advertisement = MockAdvertisementConfig(
                delay = delay,
                timeout = timeout,
                maxAdvertisingEvents = maxAdvertisingEvents,
                isAdvertisingWhenConnected = isAdvertisingWhenConnected,
                parameters = parameters,
                advertisingData = advertisingData
            )
            advertisements = advertisements?.plus(advertisement) ?: listOf(advertisement)

            // Update the set of supported PHYs.
            if (parameters is Bluetooth5AdvertisingSetParameters) {
                if (parameters.primaryPhy == PrimaryPhy.PHY_LE_CODED) {
                    supportedPhy.add(Phy.PHY_LE_CODED)
                }
                supportedPhy.add(parameters.secondaryPhy)
            }
        }

        private fun setConnectionParameters(
            name: String,
            connectionInterval: Duration,
            maxMtu: Int,
            isPhyLe2MSupported: Boolean,
            isPhyCodedSupported: Boolean,
            eventHandler: PeripheralSpecEventHandler,
            services: ServerScope.() -> Unit,
        ): Builder<ID> = apply {
            this.name = name
            this.services = ServerScopeImpl().apply(services).build()
            this.eventHandler = eventHandler
            this.connectionInterval = connectionInterval
            this.maxMtu = maxMtu.coerceIn(23, 517)
            this.isKnown = true
            if (isPhyLe2MSupported) this.supportedPhy.add(Phy.PHY_LE_2M)
            if (isPhyCodedSupported) this.supportedPhy.add(Phy.PHY_LE_CODED)
        }
    }
}