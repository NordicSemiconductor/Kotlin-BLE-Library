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

package no.nordicsemi.android.kotlin.ble.profile.csc

import no.nordicsemi.android.kotlin.ble.core.data.util.DataByteArray
import no.nordicsemi.android.kotlin.ble.core.data.util.IntFormat
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCData
import no.nordicsemi.android.kotlin.ble.profile.csc.data.CSCDataSnapshot
import no.nordicsemi.android.kotlin.ble.profile.csc.data.WheelSize
import no.nordicsemi.android.kotlin.ble.profile.csc.data.WheelSizes
import kotlin.experimental.and

class CSCDataParser {

    private var previousData: CSCDataSnapshot = CSCDataSnapshot()

    private var wheelRevolutions: Long = -1
    private var wheelEventTime: Int = -1
    private var crankRevolutions: Long = -1
    private var crankEventTime: Int = -1

    fun parse(bytes: DataByteArray, wheelSize: WheelSize = WheelSizes.default): CSCData? {
        if (bytes.size < 1) {
            return null
        }

        // Decode the new data
        var offset = 0
        val flags: Byte = bytes.getByte(offset)!!
        offset += 1

        val wheelRevPresent = (flags and 0x01).toInt() != 0
        val crankRevPreset = (flags and 0x02).toInt() != 0

        if (bytes.size < 1 + (if (wheelRevPresent) 6 else 0) + (if (crankRevPreset) 4 else 0)) {
            return null
        }

        if (wheelRevPresent) {
            wheelRevolutions = bytes.getIntValue(IntFormat.FORMAT_UINT32_LE, offset)!!.toLong() and 0xFFFFFFFFL
            offset += 4
            wheelEventTime = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!! // 1/1024 s
            offset += 2
        }

        if (crankRevPreset) {
            crankRevolutions = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!.toLong()
            offset += 2
            crankEventTime = bytes.getIntValue(IntFormat.FORMAT_UINT16_LE, offset)!!
            // offset += 2;
        }

        val wheelCircumference = wheelSize.value.toFloat()

        return CSCData(
            totalDistance = getTotalDistance(wheelSize.value.toFloat()),
            distance = getDistance(wheelCircumference, previousData),
            speed = getSpeed(wheelCircumference, previousData),
            wheelSize = wheelSize,
            cadence = getCrankCadence(previousData),
            gearRatio = getGearRatio(previousData),
        ).also {
            previousData = CSCDataSnapshot(
                wheelRevolutions,
                wheelEventTime,
                crankRevolutions,
                crankEventTime
            )
        }
    }

    private fun getTotalDistance(wheelCircumference: Float): Float {
        return wheelRevolutions.toFloat() * wheelCircumference / 1000.0f // [m]
    }

    /**
     * Returns the distance traveled since the given response was received.
     *
     * @param wheelCircumference the wheel circumference in millimeters.
     * @param previous a previous response.
     * @return distance traveled since the previous response, in meters.
     */
    private fun getDistance(
        wheelCircumference: Float,
        previous: CSCDataSnapshot
    ): Float {
        return (wheelRevolutions - previous.wheelRevolutions).toFloat() * wheelCircumference / 1000.0f // [m]
    }

    /**
     * Returns the average speed since the previous response was received.
     *
     * @param wheelCircumference the wheel circumference in millimeters.
     * @param previous a previous response.
     * @return speed in meters per second.
     */
    private fun getSpeed(
        wheelCircumference: Float,
        previous: CSCDataSnapshot
    ): Float {
        val timeDifference: Float = if (wheelEventTime < previous.wheelEventTime) {
            (65535 + wheelEventTime - previous.wheelEventTime) / 1024.0f
        } else {
            (wheelEventTime - previous.wheelEventTime) / 1024.0f
        } // [s]
        return getDistance(wheelCircumference, previous) / timeDifference // [m/s]
    }

    /**
     * Returns average wheel cadence since the previous message was received.
     *
     * @param previous a previous response.
     * @return wheel cadence in revolutions per minute.
     */
    private fun getWheelCadence(previous: CSCDataSnapshot): Float {
        val timeDifference: Float = if (wheelEventTime < previous.wheelEventTime)  {
            (65535 + wheelEventTime - previous.wheelEventTime) / 1024.0f
        } else {
            (wheelEventTime - previous.wheelEventTime) / 1024.0f
        } // [s]
        return if (timeDifference == 0f) {
            0.0f
        } else {
            (wheelRevolutions - previous.wheelRevolutions) * 60.0f / timeDifference
        }
        // [revolutions/minute];
    }

    /**
     * Returns average crank cadence since the previous message was received.
     *
     * @param previous a previous response.
     * @return crank cadence in revolutions per minute.
     */
    private fun getCrankCadence(previous: CSCDataSnapshot): Float {
        val timeDifference: Float = if (crankEventTime < previous.crankEventTime) {
            (65535 + crankEventTime - previous.crankEventTime) / 1024.0f // [s]
        } else {
            (crankEventTime - previous.crankEventTime) / 1024.0f
        } // [s]
        return if (timeDifference == 0f) {
            0.0f
        } else {
            (crankRevolutions - previous.crankRevolutions) * 60.0f / timeDifference
        }
        // [revolutions/minute];
    }

    /**
     * Returns the gear ratio (equal to wheel cadence / crank cadence).
     * @param previous a previous response.
     * @return gear ratio.
     */
    private fun getGearRatio(previous: CSCDataSnapshot): Float {
        val crankCadence = getCrankCadence(previous)
        return if (crankCadence > 0) {
            getWheelCadence(previous) / crankCadence
        } else {
            0.0f
        }
    }
}
