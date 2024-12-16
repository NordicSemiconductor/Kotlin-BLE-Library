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

package no.nordicsemi.kotlin.ble.core.android

import no.nordicsemi.kotlin.ble.core.AdvertisingDataDefinition
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Advertising data packet container for Bluetooth LE advertising.
 *
 * This represents the data to be advertised in the Advertising Data as well as the Scan Response
 * data.
 *
 * @constructor Creates an advertising data definition.
 * @param includeDeviceName Whether the device name should be included in advertise packet.
 * @param includeTxPowerLevel Whether the TX power level should be included in the
 * advertising packet.
 * @param serviceUuids A list of service UUID to advertise.
 * @param serviceSolicitationUuids Service solicitation UUID to advertise data.
 * @param serviceData Service data to be advertised.
 * @param manufacturerData Manufacturer specific data. The keys should be the Company ID as
 * defined in Assigned Numbers.
 */
@OptIn(ExperimentalUuidApi::class)
class AdvertisingDataDefinition(
    val includeDeviceName: Boolean = false,
    val includeTxPowerLevel: Boolean = false,
    serviceUuids: List<Uuid>? = null,
    val serviceSolicitationUuids: List<Uuid>? = null,
    val serviceData: Map<Uuid, ByteArray>? = null,
    val manufacturerData: Map<Int, ByteArray>? = null,
): AdvertisingDataDefinition(serviceUuids) {

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String = "AdvertisingDataDefinition(" +
        "includeDeviceName=$includeDeviceName, " +
        "includeTxPowerLevel=$includeTxPowerLevel, " +
        "serviceUuids=$serviceUuids, " +
        "serviceSolicitationUuids=$serviceSolicitationUuids, " +
        "serviceData=${serviceData?.map { entry -> 
            "${entry.key} -> 0x${entry.value.toHexString(HexFormat.UpperCase)}"}}, " +
        "manufacturerData=${manufacturerData?.map { entry -> 
            "${entry.key.toHexString(
                HexFormat { 
                    number { 
                        prefix = "0x"
                        minLength = 4
                        removeLeadingZeros = true
                    }
                }
            )} -> 0x${entry.value.toHexString(HexFormat.UpperCase)}"}}, " +
        ")"
}