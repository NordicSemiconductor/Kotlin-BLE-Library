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
import no.nordicsemi.android.kotlin.ble.core.data.util.FloatFormat
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMRecord
import no.nordicsemi.android.kotlin.ble.profile.cgm.data.CGMStatus
import no.nordicsemi.android.kotlin.ble.profile.common.CRC16

object CGMMeasurementParser {

    fun parse(bytes: DataByteArray): List<CGMRecord>? {
        if (bytes.size < 1) {
            return null
        }

        var offset = 0

        val result = mutableListOf<CGMRecord>()

        while (offset < bytes.size) {
            // Packet size
            val size: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset) ?: return null
            if (size < 6 || offset + size > bytes.size) {
                return null
            }

            // Flags
            val flags: Int = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset + 1) ?: return null
            val cgmTrendInformationPresent = flags and 0x01 != 0
            val cgmQualityInformationPresent = flags and 0x02 != 0
            val sensorWarningOctetPresent = flags and 0x20 != 0
            val sensorCalTempOctetPresent = flags and 0x40 != 0
            val sensorStatusOctetPresent = flags and 0x80 != 0
            val dataSize =
                (6 + (if (cgmTrendInformationPresent) 2 else 0) + (if (cgmQualityInformationPresent) 2 else 0)
                        + (if (sensorWarningOctetPresent) 1 else 0) + (if (sensorCalTempOctetPresent) 1 else 0)
                        + if (sensorStatusOctetPresent) 1 else 0)
            if (size != dataSize && size != dataSize + 2) {
                return null
            }
            val crcPresent = size == dataSize + 2
            if (crcPresent) {
                val expectedCrc: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset + dataSize) ?: return null
                val actualCrc: Int = CRC16.MCRF4XX(bytes.value, offset, dataSize)
                if (expectedCrc != actualCrc) {
                    continue
                }
            }
            offset += 2

            // Glucose concentration
            val glucoseConcentration: Float = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset) ?: return null
            offset += 2

            // Time offset (in minutes since Session Start)
            val timeOffset: Int = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset) ?: return null
            offset += 2

            // Sensor Status Annunciation
            var warningStatus = 0
            var calibrationTempStatus = 0
            var sensorStatus = 0
            var status: CGMStatus? = null
            if (sensorWarningOctetPresent) {
                warningStatus = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
            }
            if (sensorCalTempOctetPresent) {
                calibrationTempStatus = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
            }
            if (sensorStatusOctetPresent) {
                sensorStatus = bytes.getIntValue(IntFormat.FORMAT_UINT8, offset++) ?: return null
            }
            if (sensorWarningOctetPresent || sensorCalTempOctetPresent || sensorStatusOctetPresent) {
                status = CGMStatus(warningStatus, calibrationTempStatus, sensorStatus)
            }

            // CGM Trend Information
            var trend: Float? = null
            if (cgmTrendInformationPresent) {
                trend = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
                offset += 2
            }

            // CGM Quality Information
            var quality: Float? = null
            if (cgmQualityInformationPresent) {
                quality = bytes.getFloatValue(FloatFormat.FORMAT_SFLOAT, offset)
                offset += 2
            }

            // E2E-CRC
            if (crcPresent) {
                offset += 2
            }
            CGMRecord(
                glucoseConcentration = glucoseConcentration,
                trend = trend,
                quality = quality,
                status = status,
                timeOffset = timeOffset,
                crcPresent = crcPresent
            ).let { result.add(it) }
        }
        return result.toList()
    }
}
