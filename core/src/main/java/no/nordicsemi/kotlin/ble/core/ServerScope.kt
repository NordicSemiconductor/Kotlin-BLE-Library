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

package no.nordicsemi.kotlin.ble.core

import no.nordicsemi.kotlin.ble.core.util.fromShortUuid
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Scope for building a GATT server.
 */
@OptIn(ExperimentalUuidApi::class)
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
     *
     *       CharacteristicUserDescriptionDescriptor("Some description")
     *    }
     *    Characteristic(...)
     *    IncludedService(<Some UUID>) {
     *       Characteristic(...)
     *    }
     * }
     * ```
     *
     * @param uuid The UUID of the service.
     * @param builder Scope of the primary service.
     */
    @Suppress("FunctionName")
    fun Service(
        uuid: Uuid,
        builder: ServiceScope.() -> Unit = {}
    )

    /**
     * Declares a service with the given 16 or 32 bit short UUID.
     *
     * @param shortUuid The 16 or 32 bit short UUID of the service.
     * @param builder Scope of the primary service.
     * @see ServerScope.Service
     */
    @Suppress("FunctionName")
    fun Service(
        shortUuid: Int,
        builder: ServiceScope.() -> Unit = {}
    ) = Service(Uuid.fromShortUuid(shortUuid), builder)
}

/**
 * Scope of a GATT service.
 */
@OptIn(ExperimentalUuidApi::class)
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
     *    // Note: Client Characteristic Configuration descriptor is added automatically.
     *
     *    CharacteristicUserDescriptionDescriptor("Some description")
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
        uuid: Uuid,
        properties: List<CharacteristicProperty> = emptyList(),
        permissions: List<Permission> = emptyList(),
        builder: CharacteristicScope.() -> Unit = {}
    )

    /**
     * Declares a characteristic with the given 16 or 32 bit short UUID.
     *
     * @param shortUuid The 16 or 32 bit short UUID of the characteristic.
     * @param properties List of properties of the characteristic.
     * @param permissions The permissions of the characteristic.
     * @param builder Scope of the characteristic.
     * @see ServiceScope.Characteristic
     */
    @Suppress("FunctionName")
    fun Characteristic(
        shortUuid: Int,
        properties: List<CharacteristicProperty> = emptyList(),
        permissions: List<Permission> = emptyList(),
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(Uuid.fromShortUuid(shortUuid), properties, permissions, builder)

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
     *    // Note: Client Characteristic Configuration descriptor is added automatically.
     *
     *    CharacteristicUserDescriptionDescriptor("Some description")
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
        uuid: Uuid,
        properties: List<CharacteristicProperty>,
        permission: Permission,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(uuid, properties, listOf(permission), builder)

    /**
     * Declares a characteristic with the given 16 or 32 bit short UUID.
     *
     * @param shortUuid The 16 or 32 bit short UUID of the characteristic.
     * @param properties List of properties of the characteristic.
     * @param permission The permission of the characteristic.
     * @param builder Scope of the characteristic.
     * @see ServiceScope.Characteristic
     */
    @Suppress("FunctionName")
    fun Characteristic(
        shortUuid: Int,
        properties: List<CharacteristicProperty>,
        permission: Permission,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(Uuid.fromShortUuid(shortUuid), properties, permission, builder)

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
        uuid: Uuid,
        property: CharacteristicProperty,
        permission: Permission,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(uuid, listOf(property), listOf(permission), builder)

    /**
     * Declares a characteristic with the given 16 or 32 bit short UUID.
     *
     * @param shortUuid The 16 or 32 bit short UUID of the characteristic.
     * @param property List of properties of the characteristic.
     * @param permission The permission of the characteristic.
     * @param builder Scope of the characteristic.
     * @see ServiceScope.Characteristic
     */
    @Suppress("FunctionName")
    fun Characteristic(
        shortUuid: Int,
        property: CharacteristicProperty,
        permission: Permission,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(Uuid.fromShortUuid(shortUuid), property, permission, builder)

    /**
     * Declares a characteristic with the given UUID without permission to read or write.
     *
     * Sample code:
     * ```
     * Characteristic(
     *    uuid = <Some UUID>,
     *    property = CharacteristicProperty.NOTIFY,
     * )
     * ```
     *
     * @param uuid The UUID of the characteristic.
     * @param property List of properties of the characteristic.
     * @param builder Scope of the characteristic.
     */
    @Suppress("FunctionName")
    fun Characteristic(
        uuid: Uuid,
        property: CharacteristicProperty,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(uuid, listOf(property), emptyList(), builder)

    /**
     * Declares a characteristic with the given 16 or 32 bit short UUID
     * without permission to read or write.
     *
     * @param shortUuid The 16 or 32 bit short UUID of the characteristic.
     * @param property List of properties of the characteristic.
     * @param builder Scope of the characteristic.
     * @see ServiceScope.Characteristic
     */
    @Suppress("FunctionName")
    fun Characteristic(
        shortUuid: Int,
        property: CharacteristicProperty,
        builder: CharacteristicScope.() -> Unit = {}
    ) = Characteristic(Uuid.fromShortUuid(shortUuid), property, builder)

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
    fun IncludedService(uuid: Uuid, builder: ServiceScope.() -> Unit)

    /**
     * Declares an inner service with the given 16 or 32 bit short UUID.
     *
     * @param shortUuid The 16 or 32 bit short UUID of the inner service.
     * @param builder Scope of the inner service.
     * @see ServiceScope.IncludedService
     */
    @Suppress("FunctionName")
    fun IncludedService(shortUuid: Int, builder: ServiceScope.() -> Unit) =
        IncludedService(Uuid.fromShortUuid(shortUuid), builder)
}

/**
 * Scope of a characteristic.
 */
@OptIn(ExperimentalUuidApi::class)
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
    fun Descriptor(uuid: Uuid, permissions: List<Permission> = emptyList())

    /**
     * Declares a descriptor with the given 16 or 32 bit short UUID.
     *
     * @see CharacteristicScope.Descriptor
     */
    @Suppress("FunctionName")
    fun Descriptor(shortUuid: Int, permissions: List<Permission> = emptyList()) =
        Descriptor(Uuid.fromShortUuid(shortUuid), permissions)

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
    fun Descriptor(uuid: Uuid, permission: Permission) = Descriptor(uuid, listOf(permission))

    /**
     * Declares a descriptor with the given 16 or 32 bit short UUID.
     *
     * @see CharacteristicScope.Descriptor
     */
    @Suppress("FunctionName")
    fun Descriptor(shortUuid: Int, permission: Permission) =
        Descriptor(Uuid.fromShortUuid(shortUuid), permission)

    /**
     * Declares a Characteristic User Description descriptor.
     *
     * This descriptor is used to provide a human-readable description of the characteristic.
     * Although the CUDD is usually read-only, it may be writable. In that case, the
     * [CharacteristicProperty.EXTENDED_PROPERTIES] property and Characteristic Extended Properties
     * descriptor with `WRITABLE_AUXILIARIES` flag will be added automatically.
     *
     * @param description The human-readable description of the characteristic.
     * @param writable Whether the descriptor is writable.
     */
    @Suppress("FunctionName")
    fun CharacteristicUserDescriptionDescriptor(description: String, writable: Boolean = false)
}