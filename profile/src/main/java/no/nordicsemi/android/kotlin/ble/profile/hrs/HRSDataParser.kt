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

package no.nordicsemi.android.kotlin.ble.profile.hrs

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.hrs.data.HRSData

object HRSDataParser {

    fun parse(bytes: DataByteArray): HRSData? {

        if (bytes.size < 2) {
            return null
        }

        // Read flags

        // Read flags
        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
        val hearRateType: IntFormat = if (flags and 0x01 == 0) {
            IntFormat.FORMAT_UINT8
        } else {
            IntFormat.FORMAT_UINT16_LE
        }
        val sensorContactStatus = flags and 0x06 shr 1
        val sensorContactSupported = sensorContactStatus == 2 || sensorContactStatus == 3
        val sensorContactDetected = sensorContactStatus == 3
        val energyExpandedPresent = flags and 0x08 != 0
        val rrIntervalsPresent = flags and 0x10 != 0
        offset += 1


        // Validate packet length
        if (bytes.size < (1 + (hearRateType.value and 0x0F) + (if (energyExpandedPresent) 2 else 0) + if (rrIntervalsPresent) 2 else 0)) {
            return null
        }

        // Prepare data

        // Prepare data
        val sensorContact = if (sensorContactSupported) sensorContactDetected else false

        val heartRate: Int = bytes.getIntValue(hearRateType, offset) ?: return null
        offset += hearRateType.value and 0xF

        var energyExpanded: Int? = null
        if (energyExpandedPresent) {
            energyExpanded = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)
            offset += 2
        }

        val rrIntervals = if (rrIntervalsPresent) {
            val count: Int = (bytes.size - offset) / 2
            val intervals: MutableList<Int> = ArrayList(count)
            for (i in 0 until count) {
                intervals.add(bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!)
                offset += 2
            }
            intervals.toList()
        } else {
            emptyList()
        }

        return HRSData(heartRate, sensorContact, energyExpanded, rrIntervals)
    }
}
