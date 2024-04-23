/*
 * Copyright (c) 2023, Nordic Semiconductor
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

package no.nordicsemi.android.kotlin.ble.profile.cgm

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMStatus
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMStatusEnvelope
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

object CGMStatusParser {

    fun parse(bytes: DataByteArray): CGMStatusEnvelope? {
        if (bytes.size != 5 && bytes.size != 7) {
            return null
        }

        val timeOffset: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 0) ?: return null
        val warningStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 2) ?: return null
        val calibrationTempStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 3) ?: return null
        val sensorStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, 4) ?: return null

        val crcPresent = bytes.size == 7
        if (crcPresent) {
            val actualCrc: Int = CRC16.MCRF4XX(bytes.value, 0, 5)
            val expectedCrc: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, 5) ?: return null
            if (actualCrc != expectedCrc) {
                return null
            }
        }

        val status = CGMStatus(warningStatus, calibrationTempStatus, sensorStatus)
        return CGMStatusEnvelope(status, timeOffset, crcPresent, crcPresent)
    }
}
