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
import android.os.Build
import androidx.annotation.RequiresApi
import no.nordicsemi.android.kotlin.ble.core.data.BleGattPhy
import no.nordicsemi.android.kotlin.ble.core.scanner.BleGattPrimaryPhy

/**
 * The class provide a way to adjust advertising preferences for each Bluetooth LE
 * advertisement instance.
 *
 * @property txPowerLevel The TX power level ([BleTxPowerLevel]) for advertising.
 * @property includeTxPower Whether the TX Power will be included.
 * @property interval The advertising interval ([BleAdvertisingInterval]).
 * @property connectable Whether the advertisement will be connectable.
 * @property timeout The advertising time limit in milliseconds.
 * @property deviceName The advertising display name.
 * @property anonymous Whether the advertisement will be anonymous.
 * @property legacyMode Whether the legacy advertisement will be used.
 * @property primaryPhy The primary advertising phy ([BleGattPrimaryPhy]).
 * @property secondaryPhy The secondary advertising phy ([BleGattPhy]).
 * @property scannable Whether the advertisement will be scannable.
 *
 * @see [AdvertiseSettings](https://developer.android.com/reference/android/bluetooth/le/AdvertiseSettings)
 */
data class BleAdvertisingSettings(

    val txPowerLevel: BleTxPowerLevel? = null,
    val includeTxPower: Boolean? = null,
    val interval: BleAdvertisingInterval? = null,
    val connectable: Boolean = true,
    val timeout: Int = 0,

    @RequiresApi(Build.VERSION_CODES.O)
    val deviceName: String? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val anonymous: Boolean? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val legacyMode: Boolean = false,
    @RequiresApi(Build.VERSION_CODES.O)
    val primaryPhy: BleGattPrimaryPhy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val secondaryPhy: BleGattPhy? = null,
    @RequiresApi(Build.VERSION_CODES.O)
    val scannable: Boolean? = false,
)
