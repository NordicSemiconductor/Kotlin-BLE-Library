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

package no.nordicsemi.android.kotlin.ble.profile.gls.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class GlucoseStatus(
    val deviceBatteryLow: Boolean,
    val sensorMalfunction: Boolean,
    val sampleSizeInsufficient: Boolean,
    val stripInsertionError: Boolean,
    val stripTypeIncorrect: Boolean,
    val sensorResultLowerThenDeviceCanProcess: Boolean,
    val sensorResultHigherThenDeviceCanProcess: Boolean,
    val sensorTemperatureTooHigh: Boolean,
    val sensorTemperatureTooLow: Boolean,
    val sensorReadInterrupted: Boolean,
    val generalDeviceFault: Boolean,
    val timeFault: Boolean
) : Parcelable {

    constructor(value: Int) : this(
        deviceBatteryLow = value and 0x0001 != 0,
        sensorMalfunction = value and 0x0002 != 0,
        sampleSizeInsufficient = value and 0x0004 != 0,
        stripInsertionError = value and 0x0008 != 0,
        stripTypeIncorrect = value and 0x0010 != 0,
        sensorResultLowerThenDeviceCanProcess = value and 0x0020 != 0,
        sensorResultHigherThenDeviceCanProcess = value and 0x0040 != 0,
        sensorTemperatureTooHigh = value and 0x0080 != 0,
        sensorTemperatureTooLow = value and 0x0100 != 0,
        sensorReadInterrupted = value and 0x0200 != 0,
        generalDeviceFault = value and 0x0400 != 0,
        timeFault = value and 0x0800 != 0
    )
}
