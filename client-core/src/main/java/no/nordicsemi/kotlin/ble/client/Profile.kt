/*
 * Copyright (c) 2026, Nordic Semiconductor
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

package no.nordicsemi.kotlin.ble.client

import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.kotlin.ble.core.ConnectionState
import kotlin.uuid.Uuid

/**
 * A class representing a Bluetooth LE profile.
 *
 * The profiles are intended to separate the underlying GATT services and characteristics and the
 * application logic and device API. For example, the profile can expose methods to turn a light
 * ON or OFF, while internally it would use Bluetooth LE connection.
 *
 * @property requiredServiceUuids A list of UUIDs of required profile GATT services.
 * @property optionalServiceUuids A list of UUIDs of optional profile GATT services.
 * @property name The name of the profile. This is for convenience, used only in logging.
 * @see Peripheral.profile
 */
sealed class Profile(
    internal val requiredServiceUuids: List<Uuid>,
    internal val optionalServiceUuids: List<Uuid> = emptyList(),
    val name: String? = null,
) {
    /**
     * This method should validate if the services contain the required characteristics,
     * validate if the characteristics have expected properties, and store the references
     * to the characteristics in the class properties for later use.
     *
     * This method is called before [initialize]. It should use [require] and [first] to validate
     * the service and throw [IllegalArgumentException] or [NoSuchElementException] if validation fails.
     * In that case, a required profile will cause a disconnection with
     * [RequiredServiceNotFound][ConnectionState.Disconnected.Reason.RequiredServiceNotFound].
     *
     * #### Example
     * ```kotlin
     * override fun prepare(services: List<RemoteService>) {
     *     // Link Loss service is required.
     *     services.first { it.uuid == LINK_LOSS_SERVICE_UUID }.also { service ->
     *        // Alert Characteristic is required.
     *        linkLossCharacteristic = service.characteristics.first { it.uuid == ALERT_LEVEL_UUID }
     *
     *        require(linkLossCharacteristic.isWritable()) { "Alert level characteristic in Link Loss Service must be writable" }
     *        require(linkLossCharacteristic.isReadable()) { "Alert level characteristic in Link Loss Service must be readable" }
     *     }
     *
     *     // Immediate Alert service is optional.
     *     services.firstOrNull { it.uuid == IMMEDIATE_ALERT_SERVICE_UUID }?.also { service ->
     *        // If this service is found, the Alert Level characteristic is required.
     *        immediateAlertCharacteristic = service.characteristics.first { it.uuid == ALERT_LEVEL_UUID }
     *        require(immediateAlertCharacteristic.isWritable()) { "Alert level characteristic must be writable" }
     *     }
     *
     *     // TX Power service is optional.
     *     services.firstOrNull { it.uuid == TX_POWER_SERVICE_UUID }?.also { service ->
     *        // If this service is found, the TX Power Level characteristic is required.
     *        txPowerCharacteristic = service.characteristics.first { it.uuid == TX_POWER_UUID }
     *        require(txPowerCharacteristic.isReadable()) { "TX power characteristic must be readable" }
     *     }
     * }
     * ```
     */
    protected abstract fun prepare(services: List<RemoteService>)

    /**
     * This method should initialize the profile.
     *
     * The context of this method is the profile coroutine scope, which will automatically be
     * canceled when the device gets disconnected or the services will get invalidated.
     *
     * #### Example
     * ```kotlin
     * override suspend fun CoroutineScope.initialize() {
     *     // Subscribe to button characteristic.
     *     txCharacteristic
     *         .subscribe()
     *         .map { value -> String(value) }
     *         .onEach {
     *             println("Received: $it")
     *         }
     *         .launchIn(this)
     * }
     * ```
     */
    protected abstract suspend fun CoroutineScope.initialize()

    /**
     * Executes the [prepare] and [initialize] methods of the profile.
     *
     * @param services A list of GATT services including all the [requiredServiceUuids] and
     * some or all [optionalServiceUuids].
     * @param profileScope The coroutine scope of the profile. This scope gets canceled when the
     * services get invalidated or the device gets disconnected.
     */
    internal suspend fun execute(services: List<RemoteService>, profileScope: CoroutineScope) {
        try {
            prepare(services)
        } catch (e: NoSuchElementException) {
            throw IllegalArgumentException(e)
        }
        with(profileScope) {
            initialize()
        }
    }

    /**
     * A class representing a multiservice GATT profile.
     *
     * This is intended for profiles that have multiple GATT services, i.e. Proximity Profile.
     *
     * @param requiredServiceUuids A list of UUIDs of required profile GATT services.
     * @param optionalServiceUuids A list of UUIDs of optional profile GATT services.
     * @param name The name of the profile. This is for convenience, used only in logging.
     */
    abstract class MultiService(
        requiredServiceUuids: List<Uuid>,
        optionalServiceUuids: List<Uuid> = emptyList(),
        name: String? = null,
    ): Profile(
        requiredServiceUuids = requiredServiceUuids,
        optionalServiceUuids = optionalServiceUuids,
        name = name,
    )

    /**
     * A class representing a simple GATT profile, based on a single GATT service.
     *
     * @param serviceUuid The UUID of the GATT service.
     * @param name The name of the profile. This is for convenience, used only in logging.
     */
    abstract class Simple(
        serviceUuid: Uuid,
        name: String? = null,
    ) : Profile(
        requiredServiceUuids = listOf(serviceUuid),
        name = name,
    ) {
        final override fun prepare(services: List<RemoteService>) = prepare(services.first())

        /**
         * This method should validate if the services contain the required characteristics,
         * validate if the characteristics have expected properties, and store the references
         * to the characteristics in the class properties for later use.
         *
         * This method is called before [initialize]. It should use [require] and [first] to validate
         * the service and throw [IllegalArgumentException] or [NoSuchElementException] if validation fails.
         * In that case, a required profile will cause a disconnection with
         * [RequiredServiceNotFound][ConnectionState.Disconnected.Reason.RequiredServiceNotFound].
         *
         * ## Example
         * ```kotlin
         * override fun prepare(service: RemoteService) {
         *     buttonCharacteristic = service.characteristics.first { it.uuid == BUTTON_CHARACTERISTIC_UUID }
         *     ledCharacteristic = service.characteristics.first { it.uuid == LED_CHARACTERISTIC_UUID }
         *
         *     // Validate properties.
         *     require(buttonCharacteristic.isSubscribable()) { "Button characteristic must be subscribable." }
         *     require(ledCharacteristic.isWritable()) { "LED characteristic must be writable." }
         * }
         * ```
         */
        protected abstract fun prepare(service: RemoteService)
    }
}