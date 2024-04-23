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

package no.nordicsemi.android.kotlin.ble.profile.gls

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.date.DateTimeParser
import no.nordicsemi.android.kotlin.ble.profile.gls.data.ConcentrationUnit
import no.nordicsemi.android.kotlin.ble.profile.gls.data.GLSRecord
import no.nordicsemi.android.kotlin.ble.profile.gls.data.GlucoseStatus
import no.nordicsemi.android.kotlin.ble.profile.gls.data.RecordType
import no.nordicsemi.android.kotlin.ble.profile.gls.data.SampleLocation
import java.util.Calendar

object GlucoseMeasurementParser {

    fun parse(bytes: DataByteArray): GLSRecord? {

        if (bytes.size < 10) {
            return null
        }

        var offset = 0

        val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
        val timeOffsetPresent = flags and 0x01 != 0
        val glucoseDataPresent = flags and 0x02 != 0
        val unitMolL = flags and 0x04 != 0
        val sensorStatusAnnunciationPresent = flags and 0x08 != 0
        val contextInformationFollows = flags and 0x10 != 0

        if (bytes.size < (10 + (if (timeOffsetPresent) 2 else 0) + (if (glucoseDataPresent) 3 else 0)
                    + if (sensorStatusAnnunciationPresent) 2 else 0)
        ) {
            return null
        }

        // Required fields
        val sequenceNumber: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
        offset += 2
        val baseTime: Calendar = DateTimeParser.parse(bytes, 3) ?: return null
        offset += 7

        // Optional fields
        if (timeOffsetPresent) {
            val timeOffset: Int = bytes.getIntValue(IntFormat.FORMAT_SINT16_LE, offset) ?: return null
            offset += 2
            baseTime.add(Calendar.MINUTE, timeOffset)
        }

        var glucoseConcentration: Float? = null
        var unit: ConcentrationUnit? = null
        var type: Int? = null
        var sampleLocation: Int? = null
        if (glucoseDataPresent) {
            glucoseConcentration = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
            val typeAndSampleLocation: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 2) ?: return null
            offset += 3
            type = typeAndSampleLocation and 0x0F
            sampleLocation = typeAndSampleLocation shr 4
            unit = if (unitMolL) ConcentrationUnit.UNIT_MOLPL else ConcentrationUnit.UNIT_KGPL
        }

        var status: GlucoseStatus? = null
        if (sensorStatusAnnunciationPresent) {
            val value: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
            // offset += 2;
            status = GlucoseStatus(value)
        }

        return GLSRecord(
            sequenceNumber,
            baseTime /* with offset */,
            glucoseConcentration,
            unit,
            RecordType.createOrNull(type),
            status,
            SampleLocation.createOrNull(sampleLocation),
            contextInformationFollows
        )
    }
}
