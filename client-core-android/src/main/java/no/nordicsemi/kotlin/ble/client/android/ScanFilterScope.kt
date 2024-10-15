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

package no.nordicsemi.kotlin.ble.client.android

import no.nordicsemi.kotlin.ble.client.CentralManager
import no.nordicsemi.kotlin.ble.core.AdvertisingDataType
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Android-specific scanning filters scope.
 */
@OptIn(ExperimentalUuidApi::class)
sealed interface ScanFiltersScope: CentralManager.ScanFilterScope {

    /**
     * Adds a filter that requires at least one of the criteria to be satisfied.
     */
    fun Any(filter: DisjunctionFilterScope.() -> Unit)

    /**
     * Adds a filter that requires all the criteria to be satisfied.
     */
    @Suppress("FunctionName")
    fun All(filter: ConjunctionFilterScope.() -> Unit)

    /**
     * Filters devices by name.
     */
    @Suppress("FunctionName")
    fun Name(name: String)

    /**
     * Filters devices by name using a regular expression.
     */
    @Suppress("FunctionName")
    fun Name(regex: Regex)

    /**
     * Filters devices by MAC address.
     */
    @Suppress("FunctionName")
    fun Address(address: String)

    /**
     * Filters devices by service UUID.
     *
     * More than one service UUID can be added to the filter.
     */
    @Suppress("FunctionName")
    fun ServiceUUID(uuid: Uuid, mask: Uuid? = null)

    /**
     * Filters devices by service solicitation UUID.
     */
    @Suppress("FunctionName")
    fun ServiceSolicitationUUID(uuid: Uuid, mask: Uuid? = null)

    /**
     * Filters devices by service data.
     */
    @Suppress("FunctionName")
    fun ServiceData(uuid: Uuid, data: ByteArray, mask: ByteArray? = null)

    /**
     * Filters devices by manufacturer data.
     */
    @Suppress("FunctionName")
    fun ManufacturerData(companyId: Int, data: ByteArray, mask: ByteArray? = null)

    /**
     * Filters devices advertising a specific AD type.
     */
    @Suppress("FunctionName")
    fun Custom(type: AdvertisingDataType)

    /**
     * Filters devices advertising a specific AD type with given data.
     */
    @Suppress("FunctionName")
    fun Custom(type: AdvertisingDataType, data: ByteArray, mask: ByteArray? = null)
}

/**
 * Android-specific scanning filters scope that requires at least one of the criteria to be satisfied.
 *
 * ### Sample
 *
 * Scan for devices advertising a specific name and one of two service UUID:
 * ```
 * scan(10_000) {
 *    Name(<Some Name>)
 *    // and
 *    Any {
 *       ServiceUUID(<Some UUID>)
 *       // or
 *       ServiceUUID(<Another UUID>)
 *    }
 * }
 * ```
 */
interface DisjunctionFilterScope: ScanFiltersScope

/**
 * Android-specific scanning filters scope that requires all of the criteria to be satisfied.
 *
 * Scan for devices advertising a specific name and either the service UUID or manufacturer data:
 * ```
 * scan(10_000) {
 *    Name(<Some Name>)
 *    // and
 *    Any {
 *       All {
 *          ServiceUUID(<Some UUID>)
 *          // and
 *          ServiceData(<Some UUID>, <Data>)
 *       }
 *       // or
 *       ManufacturerData(<Company ID>, <Data>)
 *    }
 * }
 * ```
 * Mind, that setting the same filter for the second time will override the previous one.
 * For example:
 * ```
 * scan(10_000) {
 *    Name("This name will be overwritten...")
 *    Name("...by this name")
 * }
 * ```
 * To scan for any of the names, use [Any] to add a [DisjunctionFilterScope] filter:
 * ```
 * scan(10_000) {
 *    Any {
 *       Name("This name...")
 *       // or
 *       Name("...this name")
 *       // or
 *       Name("...even this name")
 *    }
 * }
 * ```
 * Adding multiple disjunction filters will cross merge the filter:
 * ```
 * scan(10_000) {
 *    ServiceUUID(<Some UUID>)
 *    // and
 *    Any {
 *       Name("This name...")
 *       // or
 *       Name("...or this name")
 *    }
 *    // and
 *    Any {
 *       ServiceData(<Some UUID>, <Data>)
 *       // or
 *       ManufacturerData(<Company ID>, <Data>)
 *    }
 * }
 * ```
 * The above filter will scan for devices that advertise the Service UUID and any of the names and
 * either the service data or manufacturer data.
 */
interface ConjunctionFilterScope: ScanFiltersScope