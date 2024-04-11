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

import no.nordicsemi.kotlin.ble.core.CharacteristicProperty
import no.nordicsemi.kotlin.ble.core.Permission
import java.util.UUID

/**
 * Scope for building a GATT server.
 */
interface ServerScope {

    /**
     * Declares a service with the given UUID.
     *
     * Sample code:
     * ```
     * Service(<Some UUID>) {
     *    Characteristic(
     *       uuid = <Some UUID>,
     *       properties = CharacteristicProperty.READ and CharacteristicProperty.NOTIFY,
     *       permission = Permission.READ,
     *    ) {
     *       // Note: Client Characteristic Configuration descriptor is added automatically.
     *    }
     *    Characteristic(...)
     * }
     * ```
     *
     * @param uuid The UUID of the service.
     * @param builder Scope of the primary service.
     */
    @Suppress("FunctionName")
    fun Service(
        uuid: UUID,
        builder: PrimaryServiceScope.() -> Unit = {}
    )
}

/**
 * Scope of a GATT service.
 */
interface ServiceScope {

    /**
     * Declares a characteristic with the given UUID.
     *
     * Sample code:
     * ```
     * Characteristic(
     *    uuid = <Some UUID>,
     *    properties = CharacteristicProperty.READ and CharacteristicProperty.WRITE,
     *    permissions = Permission.READ and Permission.WRITE,
     * ) {
     *    UserDescriptionDescriptor("Some description")
     * }
     * ```
     *
     * @param uuid The UUID of the characteristic.
     * @param properties List of properties of the characteristic.
     * @param permissions The permissions of the characteristic.
     * @param builder Scope of the characteristic.
     */
    @Suppress("FunctionName")
    fun Characteristic(
        uuid: UUID,
        properties: List<CharacteristicProperty>,
        permissions: List<Permission>,
        builder: CharacteristicScope.() -> Unit = {}
    )

    /**
     * Declares a characteristic with the given UUID.
     *
     * Sample code:
     * ```
     * Characteristic(
     *    uuid = <Some UUID>,
     *    property = CharacteristicProperty.READ and CharacteristicProperty.NOTIFY,
     *    permission = Permission.READ,
     * ) {
     *    ClientCharacteristicConfigurationDescriptor()
     *    UserDescriptionDescriptor("Some description")
     * }
     * ```
     *
     * @param uuid The UUID of the characteristic.
     * @param properties List of properties of the characteristic.
     * @param permission The permission of the characteristic.
     * @param builder Scope of the characteristic.
     */
    @Suppress("FunctionName")
    fun Characteristic(
        uuid: UUID,
        properties: List<CharacteristicProperty>,
        permission: Permission,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(uuid, properties, listOf(permission), builder)

    /**
     * Declares a characteristic with the given UUID.
     *
     * Sample code:
     * ```
     * Characteristic(
     *    uuid = <Some UUID>,
     *    property = CharacteristicProperty.READ,
     *    permission = Permission.READ,
     * )
     * ```
     *
     * @param uuid The UUID of the characteristic.
     * @param property List of properties of the characteristic.
     * @param permission The permission of the characteristic.
     * @param builder Scope of the characteristic.
     */
    @Suppress("FunctionName")
    fun Characteristic(
        uuid: UUID,
        property: CharacteristicProperty,
        permission: Permission,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(uuid, listOf(property), listOf(permission), builder)
}

/**
 * Scope of a primary service.
 */
interface PrimaryServiceScope: ServiceScope {

    /**
     * Declares an inner service with the given UUID.
     *
     * Inner services are services that are included in the primary service.
     *
     * Sample code:
     * ```
     * Service(<Some UUID>) {
     *    Characteristic(...)
     *    InnerService(<Some UUID>) {
     *       Characteristic(...)
     *       Characteristic(...)
     *    )
     * )
     * ```
     * @param uuid The UUID of the inner service.
     * @param builder Scope of the inner service.
     */
    @Suppress("FunctionName")
    fun InnerService(uuid: UUID, builder: InnerServiceScope.() -> Unit)
}

/**
 * Scope of an inner service.
 */
interface InnerServiceScope: ServiceScope

/**
 * Scope of a characteristic.
 */
interface CharacteristicScope {

    /**
     * Declares a descriptor with the given UUID.
     *
     * Sample code:
     * ```
     * Descriptor(
     *    uuid = <Some UUID>,
     *    permissions = Permission.READ and Permission.WRITE,
     * )
     * ```
     *
     * @param uuid The UUID of the descriptor.
     * @param permissions List of permissions of the descriptor.
     */
    @Suppress("FunctionName")
    fun Descriptor(uuid: UUID, permissions: List<Permission>)

    /**
     * Declares a descriptor with the given UUID.
     *
     * Sample code:
     * ```
     * Descriptor(
     *    uuid = <Some UUID>,
     *    permission = Permission.READ,
     * )
     * ```
     *
     * @param uuid The UUID of the descriptor.
     * @param permission The permission of the descriptor.
     */
    @Suppress("FunctionName")
    fun Descriptor(uuid: UUID, permission: Permission) = Descriptor(uuid, listOf(permission))

    /**
     * Declares a Characteristic User Description descriptor.
     *
     * @param description The human-readable description of the characteristic.
     * @param writable Whether the descriptor is writable.
     */
    @Suppress("FunctionName")
    fun CharacteristicUserDescriptionDescriptor(description: String, writable: Boolean = false)
}