/*
 * Copyright (c) 2022, Nordic Semiconductor
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

enum class BleTxPowerLevel {
    TX_POWER_ULTRA_LOW,
    TX_POWER_LOW,
    TX_POWER_MEDIUM,
    TX_POWER_HIGH;

    fun value(): Int {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            toLegacy()
        } else {
            toNative()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toNative(): Int {
        return when (this) {
            TX_POWER_ULTRA_LOW -> AdvertisingSetParameters.TX_POWER_ULTRA_LOW
            TX_POWER_LOW -> AdvertisingSetParameters.TX_POWER_LOW
            TX_POWER_MEDIUM -> AdvertisingSetParameters.TX_POWER_MEDIUM
            TX_POWER_HIGH -> AdvertisingSetParameters.TX_POWER_HIGH
        }
    }

    fun toLegacy(): Int {
        return when (this) {
            TX_POWER_ULTRA_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_ULTRA_LOW
            TX_POWER_LOW -> AdvertiseSettings.ADVERTISE_TX_POWER_LOW
            TX_POWER_MEDIUM -> AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM
            TX_POWER_HIGH -> AdvertiseSettings.ADVERTISE_TX_POWER_HIGH
        }
    }
}
