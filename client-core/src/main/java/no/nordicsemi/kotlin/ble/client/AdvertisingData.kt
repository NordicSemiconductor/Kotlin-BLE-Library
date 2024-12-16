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

package no.nordicsemi.kotlin.ble.client

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Advertising Data represents the data found in the Advertisement Packet and
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
interface AdvertisingData {
    val name: String?
    val serviceUuids: List<Uuid>
    val serviceSolicitationUuids: List<Uuid>
    val serviceData: Map<Uuid, ByteArray>
    val txPowerLevel: Int?
    val manufacturerData: Map<Int, ByteArray>
}