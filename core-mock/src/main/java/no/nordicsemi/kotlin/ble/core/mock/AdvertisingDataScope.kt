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

package no.nordicsemi.kotlin.ble.core.mock

import no.nordicsemi.kotlin.ble.core.AdvertisingDataScope
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Scope for building mock advertising data.
 */
@OptIn(ExperimentalUuidApi::class)
interface AdvertisingDataScope: AdvertisingDataScope {

    // TODO add Flags

    /**
     * Adds "Complete Local Name" AD type to the advertising data.
     *
     * @param name The local name to be advertised.
     */
    @Suppress("FunctionName")
    fun CompleteLocalName(name: String)

    /**
     * Adds "Shortened Local Name" AD type to the advertising data.
     *
     * @param name The local name to be advertised.
     */
    @Suppress("FunctionName")
    fun ShortenedLocalName(name: String)

    /**
     * Adds the transmission power level to the advertising data.
     *
     * The actual value of TX power level is set automatically depending on the configuration.
     */
    @Suppress("FunctionName")
    fun IncludeTxPowerLevel()

    /**
     * Adds the service UUID to the advertising data, which will be added to the AD field
     * "Service Solicitation X bit UUIDs", depending on the UUID length.
     *
     * It is possible to add multiple different services by calling this method multiple times.
     *
     * @param uuid The 128-bit service solicitation UUID.
     */
    @Suppress("FunctionName")
    fun ServiceSolicitationUuid(uuid: Uuid)

    /**
     * Adds the service UUID to the advertising data, which will be added to the AD field
     * "Service Solicitation X bit UUIDs", depending on the UUID length.
     *
     * It is possible to add multiple different services by calling this method multiple times.
     *
     * @param shortUuid The 16-bit or 32-bit service solicitation UUID.
     */
    @Suppress("FunctionName")
    fun ServiceSolicitationUuid(shortUuid: Int)

    /**
     * Adds the service data to the advertising data.
     *
     * It is possible to add multiple different services by calling this method multiple times.
     *
     * @param uuid The 128-bit service UUID.
     * @param data The service data.
     */
    @Suppress("FunctionName")
    fun ServiceData(uuid: Uuid, data: ByteArray)

    /**
     * Adds the service data to the advertising data.
     *
     * It is possible to add multiple different services by calling this method multiple times.
     *
     * @param shortUuid The 16-bit or 32-bit service UUID.
     * @param data The service data.
     */
    @Suppress("FunctionName")
    fun ServiceData(shortUuid: Int, data: ByteArray)

    /**
     * Adds the manufacturer data to the advertising data.
     *
     * It is possible to add multiple different manufacturers by calling this method multiple times.
     *
     * @param companyId The Company ID as registered in Adopted Numbers by Bluetooth SIG.
     * @param data The manufacturer specific data.
     */
    @Suppress("FunctionName")
    fun ManufacturerData(companyId: Int, data: ByteArray)

    // Fields for Bluetooth Mesh.
    //
    // Note!
    // As these fields are not available on iOS, this platform will  not report them in the
    // scan results. On Android it is possible to read them from the Raw Data.

    /**
     * Sets the Bluetooth Mesh PB ADV field in the advertising data.
     *
     * This can only be set once. If called multiple times, the last value will be used.
     *
     * It is not possible to scan for that data on iOS.
     */
    @Suppress("FunctionName")
    fun MeshPbAdv(data: ByteArray)

    /**
     * Sets the Mesh Message field in the advertising data.
     *
     * This can only be set once. If called multiple times, the last value will be used.
     *
     * It is not possible to scan for that data on iOS.
     */
    @Suppress("FunctionName")
    fun MeshMessage(data: ByteArray)

    /**
     * Sets the Mesh Beacon field in the advertising data.
     *
     * This can only be set once. If called multiple times, the last value will be used.
     *
     * It is not possible to scan for that data on iOS.
     */
    @Suppress("FunctionName")
    fun MeshBeacon(data: ByteArray)
}