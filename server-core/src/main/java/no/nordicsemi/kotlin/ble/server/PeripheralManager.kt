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

package no.nordicsemi.kotlin.ble.server

import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.kotlin.ble.core.Manager
import no.nordicsemi.kotlin.ble.core.ServerScope

/**
 * Peripheral Manager is responsible for managing the local GATT server and incoming connections.
 *
 * It may get connection events when a central device connects or disconnects.
 *
 * To make use of the Peripheral Manager, set up the GATT server configuration using [setup].
 * Defined services will be available for connected central devices.
 *
 * To handle read and write requests and send notifications and indications, obtain list of
 * [LocalService] corresponding to the connected central device using [services].
 */
interface PeripheralManager<ID, C: Central<ID>>: Manager<PeripheralManagerEngine> {

    /**
     * Creates services definition in the local GATT server.
     *
     * The services will be available for connected central devices upon connection and
     * service discovery. Each central will have access to its own instances of the services
     * (see [services]).
     *
     * Sample code:
     * ```
     * peripheralManager.setup {
     *    // Define one or more services using the DSL:
     *    Service(<Some UUID>) {
     *       // For each service, define characteristics and descriptors:
     *       Characteristic(
     *          uuid = <Some UUID>,
     *          properties = CharacteristicProperty.READ and CharacteristicProperty.NOTIFY,
     *          permissions = Permission.READ
     *       ) {
     *          // Note:
     *          // Client Characteristic Configuration descriptor is added automatically when
     *          // NOTIFY or INDICATE property is set.
     *          // Similarly, Characteristic Extended Properties descriptor is added when
     *          // RELIABLE_WRITE and/or WRITE_AUXILIARIES property is set, or when
     *          // the Characteristic User Description descriptor is added as writable.
     *
     *          // Define descriptors for the characteristic using the following syntax:
     *          Descriptor(
     *             uuid = <Some UUID>,
     *             permissions = Permission.READ
     *          )
     *
     *          // The Characteristic User Description descriptor can be added using a simple
     *          // function call:
     *          CharacteristicUserDescriptionDescriptor(
     *             description = "Some description",
     *             writable = false // default value
     *          )
     *       }
     *       // You may define more characteristics for the service:
     *       Characteristic(
     *          uuid = <Some UUID>,
     *          properties = CharacteristicProperty.WRITE,
     *          permissions = Permission.WRITE
     *       )
     *       // The primary service may contain inner services defined the same way:
     *       InnerService(<Some UUID>) {
     *          Characteristic(...)
     *       }
     *    }
     *    Service(<Some UUID>) {
     *       Characteristic(...)
     *    }
     * }
     * ```
     * You may also define characteristics as methods:
     * ```kotlin
     * fun ServerScope.HeartRateService(builder: PrimaryServiceScope.() -> Unit = {}) {
     *     Service(
     *         uuid = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb"),
     *         builder = builder
     *     )
     * }
     *
     * fun ServiceScope.HeartRateMeasurementCharacteristic() {
     *     Characteristic(
     *         uuid = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb"),
     *         properties = CharacteristicProperty.READ and CharacteristicProperty.NOTIFY,
     *         permission = Permission.READ,
     *     )
     * }
     * ```
     * And then use it in the service definition:
     * ```kotlin
     * HeartRateService {
     *    HeartRateMeasurementCharacteristic()
     * }
     *
     * @param builder The primary service definition scope.
     */
    suspend fun setup(builder: ServerScope.() -> Unit)

    /**
     * Returns a flow of local services corresponding the given central device.
     *
     * The flow is closed when the device disconnects and emits each time a local service is
     * added or removed.
     *
     * #### Important
     * These are NOT the services of the connected devices. Peripheral Manager provides access
     * only to the local services, added using [PeripheralManager.setup].
     * These services can be discovered by the connected central devices. Each central device may
     * read and write a value to the local characteristic or a descriptor and get value updates.
     * The changes are not automatically propagated to other centrals.
     *
     * To use remote services, use Central Manager and connect to the peripheral.
     */
    fun services(central: C): StateFlow<List<LocalService>>

    companion object Factory
}