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

package no.nordicsemi.kotlin.ble.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nordicsemi.kotlin.ble.core.Phy
import no.nordicsemi.kotlin.ble.core.PrimaryPhy
import org.jetbrains.annotations.Range
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * A scan result represents a single advertisement found during the scan.
 *
 * The [advertisementData] contain combined data from the Advertisement Packet and Scan Response
 * packet.
 *
 * @property peripheral The peripheral that was found.
 * @property isConnectable Whether the peripheral is connectable.
 * @property advertisementData The advertisement data (combined with scan response data,
 * if applicable).
 * @property rssi The received signal strength (RSSI), in dBm.
 * @property txPowerLevel The transmission power level, as advertised in Extended Advertising
 * packet or as AD type. `null` if not present.
 * @property primaryPhy The primary PHY used to transmit the advertisement.
 * @property secondaryPhy The secondary PHY used to transmit the advertisement, or `null` if not used.
 * @property timestamp The timestamp since boot when the scan record was observed, in milliseconds.
 */
interface ScanResult<P: Peripheral<*, *>, AD: AdvertisementData> {
    val peripheral: P
    val isConnectable: Boolean
    val advertisementData: AD
    val rssi: @Range(from = -127, to = 126) Int
    val txPowerLevel: @Range(from = -127, to = 126) Int?
    val primaryPhy: PrimaryPhy
    val secondaryPhy: Phy?
    val timestamp: Long
}

/**
 * Advertisement data represents the data found in the Advertisement Packet and
 * Scan Response combined.
 *
 * @property name The Complete Local Name or Shortened Local Name advertised, if present.
 * @property serviceUuids A list of service UUIDs advertised.
 * @property serviceSolicitationUuids A list of service solicitation UUIDs advertised.
 * @property serviceData A map of service data advertised.
 * @property txPowerLevel The transmission power level advertised, if present, in dBm.
 * @property manufacturerData The manufacturer specific data advertised, where keys are the
 * Company IDs, as registered in Adopted Numbers by Bluetooth SIG.
 */
@OptIn(ExperimentalUuidApi::class)
interface AdvertisementData {
    val name: String?
    val serviceUuids: List<Uuid>
    val serviceSolicitationUuids: List<Uuid>
    val serviceData: Map<Uuid, ByteArray>
    val txPowerLevel: Int?
    val manufacturerData: Map<Int, ByteArray>
}

/**
 * A flow which emits only one (first found) Scan Result from each discovered peripheral.
 */
fun <ID, P: Peripheral<ID, *>> Flow<ScanResult<P, *>>.distinctByPeripheral(): Flow<ScanResult<P, *>> = flow {
    val peripherals = mutableSetOf<ID>()
    collect { result ->
        if (peripherals.add(result.peripheral.identifier)) {
            emit(result)
        }
    }
}