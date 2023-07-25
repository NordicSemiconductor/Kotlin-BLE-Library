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

package no.nordicsemi.android.kotlin.ble.profile.cgm.data

data class CGMFeaturesEnvelope(
    val features: CGMFeatures,
    val type: Int,
    val sampleLocation: Int,
    val secured: Boolean,
    val crcValid: Boolean
)

class CGMFeatures(
    val calibrationSupported: Boolean,
    val patientHighLowAlertsSupported: Boolean,
    val hypoAlertsSupported: Boolean,
    val hyperAlertsSupported: Boolean,
    val rateOfIncreaseDecreaseAlertsSupported: Boolean,
    val deviceSpecificAlertSupported: Boolean,
    val sensorMalfunctionDetectionSupported: Boolean,
    val sensorTempHighLowDetectionSupported: Boolean,
    val sensorResultHighLowSupported: Boolean,
    val lowBatteryDetectionSupported: Boolean,
    val sensorTypeErrorDetectionSupported: Boolean,
    val generalDeviceFaultSupported: Boolean,
    val e2eCrcSupported: Boolean,
    val multipleBondSupported: Boolean,
    val multipleSessionsSupported: Boolean,
    val cgmTrendInfoSupported: Boolean,
    val cgmQualityInfoSupported: Boolean
) {

    constructor(value: Int) : this(
        calibrationSupported = value and 0x000001 != 0,
        patientHighLowAlertsSupported = value and 0x000002 != 0,
        hypoAlertsSupported = value and 0x000004 != 0,
        hyperAlertsSupported = value and 0x000008 != 0,
        rateOfIncreaseDecreaseAlertsSupported = value and 0x000010 != 0,
        deviceSpecificAlertSupported = value and 0x000020 != 0,
        sensorMalfunctionDetectionSupported = value and 0x000040 != 0,
        sensorTempHighLowDetectionSupported = value and 0x000080 != 0,
        sensorResultHighLowSupported = value and 0x000100 != 0,
        lowBatteryDetectionSupported = value and 0x000200 != 0,
        sensorTypeErrorDetectionSupported = value and 0x000400 != 0,
        generalDeviceFaultSupported = value and 0x000800 != 0,
        e2eCrcSupported = value and 0x001000 != 0,
        multipleBondSupported = value and 0x002000 != 0,
        multipleSessionsSupported = value and 0x004000 != 0,
        cgmTrendInfoSupported = value and 0x008000 != 0,
        cgmQualityInfoSupported = value and 0x010000 != 0
    )
}
