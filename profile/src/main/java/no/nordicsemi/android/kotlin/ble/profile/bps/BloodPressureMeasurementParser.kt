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

package no.nordicsemi.android.kotlin.ble.profile.bps

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BPMStatus
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureMeasurementData
import no.nordicsemi.android.kotlin.ble.profile.bps.data.BloodPressureType
import no.nordicsemi.android.kotlin.ble.profile.date.DateTimeParser
import java.util.Calendar

object BloodPressureMeasurementParser {

    fun parse(bytes: DataByteArray): BloodPressureMeasurementData? {
        if (bytes.size < 7) {
            return null
        }

        // First byte: flags
        var offset = 0
        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null

        // See UNIT_* for unit options
        val unit: BloodPressureType = if (flags and 0x01 == BloodPressureType.UNIT_MMHG.value) {
            BloodPressureType.UNIT_MMHG
        } else {
            BloodPressureType.UNIT_KPA
        }
        val timestampPresent = flags and 0x02 != 0
        val pulseRatePresent = flags and 0x04 != 0
        val userIdPresent = flags and 0x08 != 0
        val measurementStatusPresent = flags and 0x10 != 0

        if (bytes.size < (7
                    + (if (timestampPresent) 7 else 0) + (if (pulseRatePresent) 2 else 0)
                    + (if (userIdPresent) 1 else 0) + if (measurementStatusPresent) 2 else 0)
        ) {
            return null
        }

        // Following bytes - systolic, diastolic and mean arterial pressure
        val systolic: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset) ?: return null
        val diastolic: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset + 2) ?: return null
        val meanArterialPressure: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset + 4) ?: return null
        offset += 6

        // Parse timestamp if present
        var calendar: Calendar? = null
        if (timestampPresent) {
            calendar = DateTimeParser.parse(bytes, offset)
            offset += 7
        }

        // Parse pulse rate if present
        var pulseRate: Float? = null
        if (pulseRatePresent) {
            pulseRate = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
            offset += 2
        }

        // Read user id if present
        var userId: Int? = null
        if (userIdPresent) {
            userId = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset)
            offset += 1
        }

        // Read measurement status if present
        var status: BPMStatus? = null
        if (measurementStatusPresent) {
            val measurementStatus: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
            // offset += 2;
            status = BPMStatus(measurementStatus)
        }

        return BloodPressureMeasurementData(
            systolic,
            diastolic,
            meanArterialPressure,
            unit,
            pulseRate,
            userId,
            status,
            calendar
        )
    }
}
