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

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CGMStatusEnvelope(
    val status: CGMStatus,
    val timeOffset: Int,
    val secured: Boolean,
    val crcValid: Boolean
)

@Parcelize
data class CGMStatus(
    val sessionStopped: Boolean,
    val deviceBatteryLow: Boolean,
    val sensorTypeIncorrectForDevice: Boolean,
    val sensorMalfunction: Boolean,
    val deviceSpecificAlert: Boolean,
    val generalDeviceFault: Boolean,
    val timeSyncRequired: Boolean,
    val calibrationNotAllowed: Boolean,
    val calibrationRecommended: Boolean,
    val calibrationRequired: Boolean,
    val sensorTemperatureTooHigh: Boolean,
    val sensorTemperatureTooLow: Boolean,
    val sensorResultLowerThenPatientLowLevel: Boolean,
    val sensorResultHigherThenPatientHighLevel: Boolean,
    val sensorResultLowerThenHypoLevel: Boolean,
    val sensorResultHigherThenHyperLevel: Boolean,
    val sensorRateOfDecreaseExceeded: Boolean,
    val sensorRateOfIncreaseExceeded: Boolean,
    val sensorResultLowerThenDeviceCanProcess: Boolean,
    val sensorResultHigherThenDeviceCanProcess: Boolean
) : Parcelable {

    constructor(warningStatus: Int, calibrationTempStatus: Int, sensorStatus: Int) : this (
        sessionStopped = warningStatus and 0x01 != 0,
        deviceBatteryLow = warningStatus and 0x02 != 0,
        sensorTypeIncorrectForDevice = warningStatus and 0x04 != 0,
        sensorMalfunction = warningStatus and 0x08 != 0,
        deviceSpecificAlert = warningStatus and 0x10 != 0,
        generalDeviceFault = warningStatus and 0x20 != 0,
        timeSyncRequired = calibrationTempStatus and 0x01 != 0,
        calibrationNotAllowed = calibrationTempStatus and 0x02 != 0,
        calibrationRecommended = calibrationTempStatus and 0x04 != 0,
        calibrationRequired = calibrationTempStatus and 0x08 != 0,
        sensorTemperatureTooHigh = calibrationTempStatus and 0x10 != 0,
        sensorTemperatureTooLow = calibrationTempStatus and 0x20 != 0,
        sensorResultLowerThenPatientLowLevel = sensorStatus and 0x01 != 0,
        sensorResultHigherThenPatientHighLevel = sensorStatus and 0x02 != 0,
        sensorResultLowerThenHypoLevel = sensorStatus and 0x04 != 0,
        sensorResultHigherThenHyperLevel = sensorStatus and 0x08 != 0,
        sensorRateOfDecreaseExceeded = sensorStatus and 0x10 != 0,
        sensorRateOfIncreaseExceeded = sensorStatus and 0x20 != 0,
        sensorResultLowerThenDeviceCanProcess = sensorStatus and 0x40 != 0,
        sensorResultHigherThenDeviceCanProcess = sensorStatus and 0x80 != 0
    )
}
