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

package no.nordicsemi.android.kotlin.ble.core.advertiser

import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.AdvertisingSetParameters
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo

/**
 * Advertising interval which is tightly correlated with power consumption.
 * A helper class which is a wrapper around Native Android API.
 * It unifies parameters between different Android versions.
 *
 * @see [AdvertiseSettings](https://developer.android.com/reference/android/bluetooth/le/AdvertiseSettings) for Android < O)
 * @see [AdvertisingSetParameters](https://developer.android.com/reference/android/bluetooth/le/AdvertisingSetParameters)
 */
enum class BleAdvertisingInterval {

    /**
     * Perform high frequency, low latency advertising, around every 100ms.
     */
    INTERVAL_LOW,

    /**
     * Advertise on medium frequency, around every 250ms.
     */
    INTERVAL_MEDIUM,

    /**
     * Advertise on low frequency, around every 1000ms.
     */
    INTERVAL_HIGH;

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @RequiresApi(Build.VERSION_CODES.O)
    fun toNative(): Int {
        return when (this) {
            INTERVAL_LOW -> AdvertisingSetParameters.INTERVAL_LOW
            INTERVAL_MEDIUM -> AdvertisingSetParameters.INTERVAL_MEDIUM
            INTERVAL_HIGH -> AdvertisingSetParameters.INTERVAL_HIGH
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun toLegacy(): Int {
        return when (this) {
            INTERVAL_LOW -> AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
            INTERVAL_MEDIUM -> AdvertiseSettings.ADVERTISE_MODE_BALANCED
            INTERVAL_HIGH -> AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY
        }
    }
}
