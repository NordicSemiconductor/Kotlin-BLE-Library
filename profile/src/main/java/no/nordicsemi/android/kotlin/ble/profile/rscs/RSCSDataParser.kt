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

package no.nordicsemi.android.kotlin.ble.profile.rscs

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.LongFormat
import no.nordicsemi.android.kotlin.ble.profile.rscs.data.RSCSData

object RSCSDataParser {

    fun parse(bytes: DataByteArray): RSCSData? {
        if (bytes.size < 4) {
            return null
        }

        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
        val instantaneousStrideLengthPresent = flags and 0x01 != 0
        val totalDistancePresent = flags and 0x02 != 0
        val statusRunning = flags and 0x04 != 0
        offset += 1

        val speed = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)?.toFloat()?.let {
            it / 256f // [m/s]
        } ?: return null

        offset += 2
        val cadence: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
        offset += 1

        if (bytes.size < (4 + (if (instantaneousStrideLengthPresent) 2 else 0) + if (totalDistancePresent) 4 else 0)) {
            return null
        }

        var strideLength: Int? = null
        if (instantaneousStrideLengthPresent) {
            strideLength = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)
            offset += 2
        }

        var totalDistance: Long? = null
        if (totalDistancePresent) {
            totalDistance = bytes.getLongValue(LongFormat.FORMAT_UINT32_LE, offset)
            // offset += 4;
        }

        return RSCSData(statusRunning, speed, cadence, strideLength, totalDistance)
    }
}
